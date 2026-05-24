package uestc.b3dman.ftpclient.ui.screens.addaccount

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import uestc.b3dman.ftpclient.data.model.FtpAccount
import uestc.b3dman.ftpclient.data.repository.FtpRepository
import java.io.File
import javax.inject.Inject

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

    private val ipRegex = """^(\d{1,3})\.(\d{1,3})\.(\d{1,3})\.(\d{1,3})$""".toRegex()

    private val _validationError = MutableStateFlow<String?>(null)
    val validationError: StateFlow<String?> = _validationError.asStateFlow()

    fun clearValidationError() {
        _validationError.value = null
    }

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
        val ip = ipAndPort.split(":", limit = 2)[0]
        val portStr = ipAndPort.split(":", limit = 2).getOrNull(1)

        if (ip.isBlank()) {
            _validationError.value = "请输入服务器 IP 地址"
            return
        }
        val match = ipRegex.matchEntire(ip)
        if (match == null) {
            _validationError.value = "IP 地址格式不正确"
            return
        }
        val octets = match.groupValues.drop(1).map { it.toInt() }
        if (octets.any { it !in 0..255 }) {
            _validationError.value = "IP 地址格式不正确"
            return
        }

        val port = portStr?.toIntOrNull()
        if (portStr != null && (port == null || port !in 1..65535)) {
            _validationError.value = "端口号范围为 1-65535"
            return
        }
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