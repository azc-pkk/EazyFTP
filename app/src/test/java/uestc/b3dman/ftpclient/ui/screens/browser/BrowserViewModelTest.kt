package uestc.b3dman.ftpclient.ui.screens.browser

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import uestc.b3dman.ftpclient.data.model.FtpFileItem
import uestc.b3dman.ftpclient.data.repository.FtpRepository

@OptIn(ExperimentalCoroutinesApi::class)
class BrowserViewModelTest {

    private val repository: FtpRepository = mockk(relaxed = true)

    private val rootFiles = listOf(
        FtpFileItem("Documents", true, 1000, 0, "/Documents"),
        FtpFileItem("readme.txt", false, 2000, 500, "/readme.txt"),
        FtpFileItem("data.bin", false, 3000, 2048, "/data.bin")
    )

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun setupViewModel(): BrowserViewModel {
        coEvery { repository.getFiles(any()) } returns emptyList()
        coEvery { repository.getFiles("/") } returns rootFiles
        return BrowserViewModel(repository)
    }

    @Test
    fun `initial sort type is NAME`() {
        Dispatchers.setMain(StandardTestDispatcher())
        val vm = setupViewModel()
        assertEquals(SortType.NAME, vm.currentSortType.value)
    }

    @Test
    fun `initial search is inactive`() {
        Dispatchers.setMain(StandardTestDispatcher())
        val vm = setupViewModel()
        assertFalse(vm.isSearchActive.value)
    }

    @Test
    fun `setSortType changes sort type`() {
        Dispatchers.setMain(StandardTestDispatcher())
        val vm = setupViewModel()
        assertEquals(SortType.NAME, vm.currentSortType.value)
        vm.setSortType(SortType.SIZE)
        assertEquals(SortType.SIZE, vm.currentSortType.value)
        vm.setSortType(SortType.TIME)
        assertEquals(SortType.TIME, vm.currentSortType.value)
    }

    @Test
    fun `toggleSearch activates and deactivates search`() {
        Dispatchers.setMain(StandardTestDispatcher())
        val vm = setupViewModel()
        assertFalse(vm.isSearchActive.value)
        vm.toggleSearch()
        assertTrue(vm.isSearchActive.value)
        vm.toggleSearch()
        assertFalse(vm.isSearchActive.value)
        assertEquals("", vm.searchQuery.value)
    }

    @Test
    fun `updateSearchQuery updates query`() {
        Dispatchers.setMain(StandardTestDispatcher())
        val vm = setupViewModel()
        vm.toggleSearch()
        vm.updateSearchQuery("test")
        assertEquals("test", vm.searchQuery.value)
    }

    @Test
    fun `createFolder dialog open and close`() {
        Dispatchers.setMain(StandardTestDispatcher())
        val vm = setupViewModel()
        assertFalse(vm.showCreateFolderDialog.value)
        vm.onCreateFolderClick()
        assertTrue(vm.showCreateFolderDialog.value)
        vm.onCreateFolderDismiss()
        assertFalse(vm.showCreateFolderDialog.value)
    }
}
