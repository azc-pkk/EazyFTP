package uestc.b3dman.ftpclient.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uestc.b3dman.ftpclient.data.model.DownloadHistoryEntry
import uestc.b3dman.ftpclient.data.model.DownloadStatus
import uestc.b3dman.ftpclient.data.model.FtpFileItem
import uestc.b3dman.ftpclient.data.repository.FtpRepository
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: FtpRepository
) : ViewModel() {
    private val _accountId = MutableStateFlow<Int?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val history: StateFlow<List<DownloadHistoryEntryUiState>> = _accountId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList())
            else repository.getDownloadHistory(id)
        }
        .map { list ->
            list.map { it.toUiState() }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _retryTargetEntry = MutableStateFlow<DownloadHistoryEntry?>(null)
    val retryTargetEntry: StateFlow<DownloadHistoryEntry?> = _retryTargetEntry.asStateFlow()

    fun setAccount(id: Int) {
        _accountId.value = id
    }

    fun onClickEntry(entry: DownloadHistoryEntryUiState) {
        when (entry.status) {
            DownloadStatus.SUCCESS -> repository.openDownloadedFile(entry.localPath)
            DownloadStatus.FAILED -> {
                _retryTargetEntry.value = DownloadHistoryEntry(
                    id = entry.id,
                    fileName = entry.fileName,
                    remotePath = entry.remotePath,
                    localPath = entry.localPath,
                    fileSize = 0,
                    downloadTime = 0,
                    accountId = entry.accountId,
                    status = DownloadStatus.FAILED,
                )
            }
            else -> {}
        }
    }

    fun onRetryDismiss() {
        _retryTargetEntry.value = null
    }

    fun onRetryConfirm() {
        val entry = _retryTargetEntry.value ?: return
        _retryTargetEntry.value = null
        viewModelScope.launch {
            val fileItem = FtpFileItem(
                name = entry.fileName,
                isFolder = false,
                lastUpdateTime = 0,
                size = entry.fileSize,
                fullPath = entry.remotePath
            )
            repository.downloadFile(entry.accountId, fileItem)
        }
    }
}
