package uestc.b3dman.ftpclient.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uestc.b3dman.ftpclient.data.model.FtpAccount
import uestc.b3dman.ftpclient.data.repository.FtpRepository
import javax.inject.Inject

sealed class LoginResult {
    data object Success : LoginResult()
    data object Failed : LoginResult()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: FtpRepository
) : ViewModel() {

    val accounts = repository.savedAccounts

    private val _isLoggingIn = MutableStateFlow(false)
    val isLoggingIn = _isLoggingIn.asStateFlow()

    private val _loginResult = MutableStateFlow<LoginResult?>(null)
    val loginResult = _loginResult.asStateFlow()

    fun performLogin(account: FtpAccount) {
        viewModelScope.launch {
            _isLoggingIn.value = true
            val result = repository.login(account)
            _isLoggingIn.value = false
            _loginResult.value = if (result.isSuccess) LoginResult.Success else LoginResult.Failed
        }
    }

    fun clearLoginResult() {
        _loginResult.value = null
    }

    fun switchAccount(account: FtpAccount) {
        viewModelScope.launch {
            repository.updateLastLoginTime(account)
        }
    }

    fun deleteAccount(account: FtpAccount) {
        viewModelScope.launch {
            repository.deleteAccount(account)
        }
    }
}