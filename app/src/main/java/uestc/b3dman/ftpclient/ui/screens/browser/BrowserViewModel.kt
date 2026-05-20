package uestc.b3dman.ftpclient.ui.screens.browser

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    var accountId: Int = -1

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
    val files: StateFlow<List<FtpFileUiState>> = _files.map { list ->
        list.map { it.toUiState() }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

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
        }
    }

    fun onEnter(folder: String) {
        _pathStack.value += folder
    }

    fun onBack(onExit: () -> Unit) {
        if (currentPathString.value == "/") {
            onExit()
        } else {
            val currentStack = _pathStack.value
            _pathStack.value = currentStack.dropLast(1)
        }
    }

    fun onAction(action: String, file: FtpFileUiState?) {
        val rawFileItem = _files.value.find { it.name == file?.name } ?: return
        when (action) {
            "Download" -> {
                viewModelScope.launch(Dispatchers.IO) {
                    repository.downloadFile(accountId, rawFileItem)
                }
            }
            "Rename" -> {
                // TODO: 重命名文件
            }
            "Delete" -> {
                // TODO: 删除文件
            }
            "Share" -> {
                // TODO: 分享文件
            }
        }
    }
}