package uestc.b3dman.ftpclient.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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

    fun setAccount(id: Int) {
        _accountId.value = id
    }
}