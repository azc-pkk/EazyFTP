package uestc.b3dman.ftpclient.ui.screens.addaccount

import android.net.Uri
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import uestc.b3dman.ftpclient.data.repository.FtpRepository

@OptIn(ExperimentalCoroutinesApi::class)
class AddAccountViewModelTest {

    private val repository: FtpRepository = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: AddAccountViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = AddAccountViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has empty fields`() = runTest {
        assertEquals("", viewModel.ipAndPort.value)
        assertEquals("", viewModel.username.value)
        assertEquals("", viewModel.password.value)
        assertEquals("", viewModel.alias.value)
        assertNull(viewModel.avatarUri.value)
    }

    @Test
    fun `updateIpAndPort updates state`() = runTest {
        viewModel.updateIpAndPort("192.168.1.1:21")
        assertEquals("192.168.1.1:21", viewModel.ipAndPort.value)
    }

    @Test
    fun `updateUsername updates state`() = runTest {
        viewModel.updateUsername("testuser")
        assertEquals("testuser", viewModel.username.value)
    }

    @Test
    fun `updatePassword updates state`() = runTest {
        viewModel.updatePassword("secret")
        assertEquals("secret", viewModel.password.value)
    }

    @Test
    fun `updateAlias updates state`() = runTest {
        viewModel.updateAlias("My Server")
        assertEquals("My Server", viewModel.alias.value)
    }

    @Test
    fun `addAccount with valid ip without port`() = runTest {
        viewModel.updateIpAndPort("192.168.1.100")
        var callbackInvoked = false

        viewModel.addAccount { callbackInvoked = true }
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(callbackInvoked)
        coVerify {
            repository.saveAccountWithAvatar(
                match { it.ip == "192.168.1.100" && it.port == 21 },
                null
            )
        }
    }

    @Test
    fun `addAccount with valid ip and port`() = runTest {
        viewModel.updateIpAndPort("10.0.0.1:2121")
        var callbackInvoked = false

        viewModel.addAccount { callbackInvoked = true }
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(callbackInvoked)
        coVerify {
            repository.saveAccountWithAvatar(
                match { it.ip == "10.0.0.1" && it.port == 2121 },
                null
            )
        }
    }

    @Test
    fun `addAccount sets validation error for blank ip`() = runTest {
        viewModel.updateIpAndPort("")
        var callbackInvoked = false

        viewModel.addAccount { callbackInvoked = true }
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.validationError.value?.contains("IP") == true)
        assertTrue(!callbackInvoked)
    }

    @Test
    fun `addAccount sets validation error for invalid ip format`() = runTest {
        viewModel.updateIpAndPort("999.999.999.999")
        var callbackInvoked = false

        viewModel.addAccount { callbackInvoked = true }
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.validationError.value?.contains("IP") == true)
    }

    @Test
    fun `addAccount sets validation error for completely invalid ip`() = runTest {
        viewModel.updateIpAndPort("abc.def.ghi")
        var callbackInvoked = false

        viewModel.addAccount { callbackInvoked = true }
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.validationError.value?.contains("IP") == true)
    }

    @Test
    fun `addAccount sets validation error for invalid port`() = runTest {
        viewModel.updateIpAndPort("192.168.1.1:99999")
        var callbackInvoked = false

        viewModel.addAccount { callbackInvoked = true }
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.validationError.value?.contains("\u7AEF\u53E3") == true)
    }

    @Test
    fun `addAccount sets validation error for port zero`() = runTest {
        viewModel.updateIpAndPort("192.168.1.1:0")
        var callbackInvoked = false

        viewModel.addAccount { callbackInvoked = true }
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.validationError.value?.contains("\u7AEF\u53E3") == true)
    }

    @Test
    fun `addAccount uses anonymous when username blank`() = runTest {
        viewModel.updateIpAndPort("192.168.1.1")
        viewModel.updateUsername("")
        var callbackInvoked = false

        viewModel.addAccount { callbackInvoked = true }
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            repository.saveAccountWithAvatar(
                match { it.userName == "anonymous" },
                null
            )
        }
    }

    @Test
    fun `addAccount uses ip as alias when alias blank`() = runTest {
        viewModel.updateIpAndPort("10.0.0.5")
        viewModel.updateAlias("")
        var callbackInvoked = false

        viewModel.addAccount { callbackInvoked = true }
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            repository.saveAccountWithAvatar(
                match { it.alias == "10.0.0.5" },
                null
            )
        }
    }

    @Test
    fun `clearValidationError clears error`() = runTest {
        viewModel.updateIpAndPort("invalid")
        var callbackInvoked = false
        viewModel.addAccount { callbackInvoked = true }
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.validationError.value != null)

        viewModel.clearValidationError()

        assertNull(viewModel.validationError.value)
    }

    @Test
    fun `updateAvatarUri updates state`() = runTest {
        val uri: Uri = mockk()
        viewModel.updateAvatarUri(uri)
        assertEquals(uri, viewModel.avatarUri.value)
    }
}
