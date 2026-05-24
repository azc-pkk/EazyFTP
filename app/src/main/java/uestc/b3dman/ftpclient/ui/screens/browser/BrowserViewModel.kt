package uestc.b3dman.ftpclient.ui.screens.browser

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uestc.b3dman.ftpclient.data.model.FtpFileItem
import uestc.b3dman.ftpclient.data.repository.FtpRepository
import javax.inject.Inject

@HiltViewModel
class BrowserViewModel @Inject constructor(
    private val repository: FtpRepository
) : ViewModel() {

    enum class SortType(val displayName: String) {
        NAME("按名称"),
        SIZE("按大小"),
        TIME("按时间")
    }

    var accountId: Int = -1

    private val _sortType = MutableStateFlow(SortType.NAME)

    private val _searchQuery = MutableStateFlow("")

    private val _pathStack = MutableStateFlow(listOf("/"))
    val pathStack = _pathStack.asStateFlow()
// FIXME
    val currentPathString: StateFlow<String> = _pathStack
        .map { pathList ->
            val filtered = pathList.filter { it != "/" }
            "/" + filtered.joinToString("/")
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "/"
        )

    private val _files = MutableStateFlow<List<FtpFileItem>>(emptyList())
    val files: StateFlow<List<FtpFileUiState>> = combine(
        _files,
        _sortType,
        _searchQuery
    ) { list, sortType, query ->
        val filtered = if (query.isEmpty()) list
        else list.filter { it.name.contains(query, ignoreCase = true) }
        filtered.sortedWith(
            compareByDescending<FtpFileItem> { it.isFolder }
                .then(compareBy { it.name.lowercase() })
                .then(
                    when (sortType) {
                        SortType.NAME -> compareBy { 0 }
                        SortType.SIZE -> compareByDescending { it.size }
                        SortType.TIME -> compareByDescending { it.lastUpdateTime }
                    }
                )
        ).map { it.toUiState() }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val currentSortType: StateFlow<SortType> = _sortType.asStateFlow()

    fun setSortType(sortType: SortType) {
        _sortType.value = sortType
    }
    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive.asStateFlow()
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleSearch() {
        _isSearchActive.value = !_isSearchActive.value
        if (!_isSearchActive.value) {
            _searchQuery.value = ""
        }
    }

    // 路径改变自动调用 getFiles
    init {
        viewModelScope.launch {
            currentPathString.collect {
                getFiles()
            }
        }
    }

    fun getFiles() {
        viewModelScope.launch {
            _files.value = repository.getFiles(currentPathString.value)
            Log.d("BrowserViewModel", "getFiles: getting files for path ${currentPathString.value}, got ${_files.value.size} items")
        }
    }

    fun uploadFile(uri: Uri) {
        viewModelScope.launch {
            repository.uploadFile(currentPathString.value, uri)
            getFiles()
        }
    }

    fun onEnter(folder: String) {
        _pathStack.value += folder
        _isSearchActive.value = false
        _searchQuery.value = ""
    }

    fun onBack(onExit: () -> Unit) {
        if (currentPathString.value == "/") {
            onExit()
        } else {
            val currentStack = _pathStack.value
            _pathStack.value = currentStack.dropLast(1)
        }
    }

    private val _renameTargetFile = MutableStateFlow<FtpFileItem?>(null)
    val renameTargetFile: StateFlow<FtpFileItem?> = _renameTargetFile.asStateFlow()

    private val _showCreateFolderDialog = MutableStateFlow(false)
    val showCreateFolderDialog: StateFlow<Boolean> = _showCreateFolderDialog.asStateFlow()

    fun onAction(action: String, file: FtpFileUiState?) {
        val rawFileItem = _files.value.find { it.fullPath == file?.fullPath } ?: return
        when (action) {
            "Download" -> {
                viewModelScope.launch {
                    repository.downloadFile(accountId, rawFileItem)
                }
            }
            "Rename" -> {
                _renameTargetFile.value = rawFileItem
            }
            "Delete" -> {
                _deleteTargetFile.value = rawFileItem
            }
            "Share" -> {
                // TODO: 分享文件
            }
        }
    }

    fun onRenameDismiss() {
        _renameTargetFile.value = null
    }

    fun onRenameConfirm(newName: String) {
        val file = _renameTargetFile.value ?: return
        _renameTargetFile.value = null
        viewModelScope.launch {
            val parentPath = currentPathString.value
            val fromPath = file.fullPath
            val toPath = if (parentPath.endsWith("/")) parentPath + newName else "$parentPath/$newName"
            repository.renameFile(fromPath, toPath)
            getFiles()
        }
    }

    private val _deleteTargetFile = MutableStateFlow<FtpFileItem?>(null)
    val deleteTargetFile: StateFlow<FtpFileItem?> = _deleteTargetFile.asStateFlow()

    fun onDeleteDismiss() {
        _deleteTargetFile.value = null
    }

    fun onDeleteConfirm() {
        val file = _deleteTargetFile.value ?: return
        _deleteTargetFile.value = null
        viewModelScope.launch {
            repository.deleteFile(file.fullPath)
            getFiles()
        }
    }

    fun onCreateFolderClick() {
        _showCreateFolderDialog.value = true
    }

    fun onCreateFolderDismiss() {
        _showCreateFolderDialog.value = false
    }

    fun onCreateFolderConfirm(folderName: String) {
        _showCreateFolderDialog.value = false
        viewModelScope.launch {
            val parentPath = currentPathString.value
            val fullPath = if (parentPath.endsWith("/")) parentPath + folderName else "$parentPath/$folderName"
            repository.createFolder(fullPath)
            getFiles()
        }
    }
}