package uestc.b3dman.ftpclient.ui.screens.addaccount

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uestc.b3dman.ftpclient.data.model.FtpAccount
import uestc.b3dman.ftpclient.data.repository.FtpRepository
import java.io.File
import javax.inject.Inject

@HiltViewModel
class AddAccountViewModel @Inject constructor(
    private val repository: FtpRepository,
) : ViewModel() {

    private val _ipAndPort = MutableStateFlow("")
    val ipAndPort: StateFlow<String> = _ipAndPort.asStateFlow()

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _alias = MutableStateFlow("")
    val alias: StateFlow<String> = _alias.asStateFlow()

    private val _avatarUri = MutableStateFlow<Uri?>(null)
    val avatarUri: StateFlow<Uri?> = _avatarUri.asStateFlow()

    private var isEditMode = false
    private var accountId = -1

    private val ipRegex = """^(\d{1,3})\.(\d{1,3})\.(\d{1,3})\.(\d{1,3})$""".toRegex()

    private val _validationError = MutableStateFlow<String?>(null)
    val validationError: StateFlow<String?> = _validationError.asStateFlow()

    fun clearValidationError() {
        _validationError.value = null
    }

    fun updateIpAndPort(value: String) { _ipAndPort.value = value }
    fun updateUsername(value: String) { _username.value = value }
    fun updatePassword(value: String) { _password.value = value }
    fun updateAlias(value: String) { _alias.value = value }
    fun updateAvatarUri(uri: Uri?) { _avatarUri.value = uri }

    fun loadAccount(id: Int) {
        if (id == -1) return
        isEditMode = true
        accountId = id
        viewModelScope.launch {
            repository.getAccountById(id)?.let { account ->
                _ipAndPort.value = if (account.port == 21) account.ip else "${account.ip}:${account.port}"
                _username.value = account.userName
                _password.value = account.password
                _alias.value = account.alias
                _avatarUri.value = account.avatarPath?.let { Uri.fromFile(File(it)) }
            }
        }
    }

    fun addAccount(onSuccess: () -> Unit) {
        val ip = _ipAndPort.value.split(":", limit = 2)[0]
        val portStr = _ipAndPort.value.split(":", limit = 2).getOrNull(1)

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
                ip = _ipAndPort.value.split(":", limit = 2)[0],
                port = _ipAndPort.value.split(":", limit = 2).getOrNull(1)?.toIntOrNull() ?: 21,
                userName = _username.value.ifBlank { "anonymous" },
                password = _password.value,
                alias = _alias.value.ifBlank { _ipAndPort.value.split(":", limit = 2)[0] },
                lastLoginTime = System.currentTimeMillis()
            )
            if (isEditMode) repository.updateAccountWithAvatar(newAccount, _avatarUri.value)
            else repository.saveAccountWithAvatar(newAccount, _avatarUri.value)
            onSuccess()
        }
    }
}
