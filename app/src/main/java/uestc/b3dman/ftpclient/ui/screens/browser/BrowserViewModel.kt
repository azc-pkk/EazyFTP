package uestc.b3dman.ftpclient.ui.screens.browser

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

    private val _pathStack = MutableStateFlow(listOf("/"))
    val pathStack = _pathStack.asStateFlow()

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
    val files = _files.asStateFlow()

    // 路径改变自动调用 getFiles
    init {
        viewModelScope.launch {
            currentPathString.collect {
                getFiles()
            }
        }
    }

    fun getFiles() {
        viewModelScope.launch(Dispatchers.IO) {
            _files.value = repository.getFiles(currentPathString.value)
            Log.d("BrowserViewModel", "getFiles: getting files for path ${currentPathString.value}, got ${_files.value.size} items")
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

    fun onAction(action: String, file: FtpFileItem?) {
        when (action) {
            "Download" -> {
                if (file != null) {
                    viewModelScope.launch(Dispatchers.IO) {
                        repository.downloadFile(file.fullPath, file.name)
                    }
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