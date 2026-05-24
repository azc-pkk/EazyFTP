package uestc.b3dman.ftpclient.ui.screens.addaccount

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.viewModelScope
import javax.inject.Inject
import kotlinx.coroutines.launch
import uestc.b3dman.ftpclient.data.model.FtpAccount
import uestc.b3dman.ftpclient.data.repository.FtpRepository
import java.io.File

@HiltViewModel
class AddAccountViewModel @Inject constructor(
    private val repository: FtpRepository,
) : ViewModel() {

    var ipAndPort by mutableStateOf("")
    var username by mutableStateOf("")
    var password by mutableStateOf("")
    var alias by mutableStateOf("")
    var avatarUri by mutableStateOf<Uri?>(null)

    private var isEditMode = false
    private var accountId = -1

    fun loadAccount(id: Int) {
        if (id == -1) return
        isEditMode = true
        accountId = id
        viewModelScope.launch {
            repository.getAccountById(id)?.let { account ->
                ipAndPort = if (account.port == 21) account.ip else "${account.ip}:${account.port}"
                username = account.userName
                password = account.password
                alias = account.alias
                avatarUri = account.avatarPath?.let { File(it).toUri() }
            }
        }
    }

    fun addAccount(onSuccess: () -> Unit) {
        // TODO: 各字段校验
        viewModelScope.launch {
            val newAccount = FtpAccount(
                id = if (isEditMode) accountId else 0,
                ip = ipAndPort.split(":", limit = 2)[0],
                port = ipAndPort.split(":", limit = 2).getOrNull(1)?.toIntOrNull() ?: 21,
                userName = username.ifBlank { "anonymous" },
                password = password,
                alias = alias.ifBlank { ipAndPort.split(":", limit = 2)[0] },
                lastLoginTime = System.currentTimeMillis()
            )
            if (isEditMode) repository.updateAccountWithAvatar(newAccount, avatarUri)
            else repository.saveAccountWithAvatar(newAccount, avatarUri)
            onSuccess()
        }
    }
}