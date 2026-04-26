package uestc.b3dman.ftpclient.ui.screens.addaccount

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uestc.b3dman.ftpclient.data.model.FtpAccount
import uestc.b3dman.ftpclient.data.repository.FtpRepository
import uestc.b3dman.ftpclient.utils.FileUtils

@HiltViewModel
class AddAccountViewModel @Inject constructor(
    private val repository: FtpRepository,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    fun addAccount(ip: String, port: Int, userName: String, password: String, alias: String, avatarUri: Uri?, onSuccess: () -> Unit) {
        // TODO: 各字段校验
        viewModelScope.launch(Dispatchers.IO) {
            val avatarPath = avatarUri?.let { uri ->
                // 将 URI 转换为本地文件路径
                FileUtils.saveUriToInternalStorage(context, uri)
            }
            val newAccount = FtpAccount(
                ip = ip,
                port = port,
                userName = userName,
                password = password,
                alias = alias,
                avatarPath = avatarPath,
                lastLoginTime = System.currentTimeMillis()
            )
            repository.saveAccount(newAccount)
            // 切换回主线程调用 onSuccess
            withContext(Dispatchers.Main) {
                onSuccess()
            }
        }
    }
}