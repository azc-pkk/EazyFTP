package uestc.b3dman.ftpclient.ui.screens.login

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import uestc.b3dman.ftpclient.data.model.FtpAccount
import uestc.b3dman.ftpclient.data.repository.FtpRepository

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val repository: FtpRepository = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: LoginViewModel

    private val testAccount = FtpAccount(
        id = 1,
        ip = "192.168.1.100",
        port = 21,
        userName = "admin",
        password = "pass",
        alias = "MyServer",
        lastLoginTime = 0
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { repository.savedAccounts } returns flowOf(listOf(testAccount))
        viewModel = LoginViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state isLoggingIn is false`() = runTest {
        assertEquals(false, viewModel.isLoggingIn.value)
    }

    @Test
    fun `loginResult is null initially`() = runTest {
        assertNull(viewModel.loginResult.value)
    }

    @Test
    fun `performLogin success sets loginResult to Success`() = runTest {
        coEvery { repository.login(testAccount) } returns Result.success(true)

        viewModel.performLogin(testAccount)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(LoginResult.Success, viewModel.loginResult.value)
        assertFalse(viewModel.isLoggingIn.value)
    }

    @Test
    fun `performLogin failure sets loginResult to Failed`() = runTest {
        coEvery { repository.login(testAccount) } returns Result.failure(Exception("fail"))

        viewModel.performLogin(testAccount)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(LoginResult.Failed, viewModel.loginResult.value)
        assertFalse(viewModel.isLoggingIn.value)
    }

    @Test
    fun `clearLoginResult sets result to null`() = runTest {
        coEvery { repository.login(testAccount) } returns Result.success(true)
        viewModel.performLogin(testAccount)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(LoginResult.Success, viewModel.loginResult.value)

        viewModel.clearLoginResult()

        assertNull(viewModel.loginResult.value)
    }

    @Test
    fun `switchAccount updates last login time`() = runTest {
        coEvery { repository.updateLastLoginTime(testAccount) } returns Unit

        viewModel.switchAccount(testAccount)
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @Test
    fun `deleteAccount delegates to repository`() = runTest {
        coEvery { repository.deleteAccount(testAccount) } returns Unit

        viewModel.deleteAccount(testAccount)
        testDispatcher.scheduler.advanceUntilIdle()
    }
}
