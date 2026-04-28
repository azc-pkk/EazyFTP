package uestc.b3dman.ftpclient.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uestc.b3dman.ftpclient.data.model.FtpAccount
import uestc.b3dman.ftpclient.data.repository.FtpRepository
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: FtpRepository
) : ViewModel() {

    // 观察账号列表（自动对应 UI 的 1.png 或 2.png 切换）
    val accounts = repository.savedAccounts

    // 登录状态
    private val _isLoggingIn = MutableStateFlow(false)
    val isLoggingIn = _isLoggingIn.asStateFlow()

    // 执行登录
    fun performLogin(account: FtpAccount, onSuccess: () -> Unit, onFailed: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoggingIn.value = true
            val result = repository.login(account)
            _isLoggingIn.value = false
            if (result.isSuccess) {
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            }
        }
    }

    fun switchAccount(account: FtpAccount) {
        viewModelScope.launch {
            repository.updateLastLoginTime(account)
        }
    }
}