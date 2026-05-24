package uestc.b3dman.ftpclient.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PersonAddAlt
import androidx.compose.material.icons.filled.PersonAddAlt1
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import uestc.b3dman.ftpclient.data.model.FtpAccount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigateToBrowser: (Int) -> Unit,
    onNavigateToAddAccount: () -> Unit,
    onNavigateToEditAccount: (Int) -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val accounts by viewModel.accounts.collectAsState(initial = emptyList())
    val isLoggingIn by viewModel.isLoggingIn.collectAsState()
    val loginResult by viewModel.loginResult.collectAsState()
    val recentAccount = accounts.maxByOrNull { it.lastLoginTime }

    // 控制底部弹窗显示
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    // 删除账号
    var accountToDelete by remember { mutableStateOf<FtpAccount?>(null) }

    // 登录失败
    var loginFailed by remember { mutableStateOf(false) }

    LaunchedEffect(loginResult) {
        when (loginResult) {
            is LoginResult.Success -> {
                viewModel.clearLoginResult()
                recentAccount?.let { onNavigateToBrowser(it.id) }
            }
            is LoginResult.Failed -> {
                viewModel.clearLoginResult()
                loginFailed = true
            }
            null -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(80.dp))

        // 标题
        Text(
            text = "Easy FTP",
            fontSize = 32.sp,
            fontWeight = FontWeight.Medium,
//            modifier = Modifier.clickable { hasHistory = !hasHistory } // 点击切换状态演示
        )

        Spacer(modifier = Modifier.weight(0.4f))

        if (recentAccount == null) {
            // --- 情况 1: 展现 1.png 的内容 ---
            LoginContentEmpty(onAddAccount = onNavigateToAddAccount)
        } else {
            // --- 情况 2: 展现 2.png 的内容 ---
            // 将 accounts 按 lastLoginTime 排序，取最近登录的一个账号展示
            LoginContentWithHistory(
                account = recentAccount,
                isLoggingIn = isLoggingIn,
                onLogin = { viewModel.performLogin(recentAccount) },
            )
        }

        Spacer(modifier = Modifier.weight(0.6f))

        // 底部操作栏 (仅在有历史时显示，或者根据需求常驻)
        if (recentAccount != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BottomIconAction(label = "添加账号", icon = Icons.Default.PersonAddAlt1, onClick = onNavigateToAddAccount)
                BottomIconAction(label = "切换账号", icon = Icons.Default.SwitchAccount, onClick = { showSheet = true })
            }
        }

        // 切换账号
        if (showSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSheet = false },
                sheetState = sheetState,
                containerColor = Color.White,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                AccountListSheetContent(
                    accounts = accounts,
                    onAccountSelected = { account ->
                        viewModel.switchAccount(account) // 更新 ViewModel 状态
                        showSheet = false // 关闭弹窗
                    },
                    onEditClick = { account ->
                        showSheet = false
                        onNavigateToEditAccount(account.id)
                    },
                    onDeleteClick = { account ->
                        accountToDelete = account
                        showSheet = false
                    }
                )
            }
        }

        // 删除弹窗
        if (accountToDelete != null) {
            AlertDialog(
                onDismissRequest = { accountToDelete = null },
                title = { Text("确认删除") },
                text = { Text("确定要删除账号 ${accountToDelete?.userName + "@" + accountToDelete?.alias} 吗？") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteAccount(accountToDelete!!)
                            accountToDelete = null
                        }
                    ) { Text("删除", color = Color.Red) }
                },
                dismissButton = {
                    TextButton(onClick = { accountToDelete = null }) { Text("取消") }
                }
            )
        }

        if (loginFailed) {
            AlertDialog(
                onDismissRequest = { loginFailed = false },
                title = { Text("登录失败") },
                text = { Text("登录失败，请检查账号信息") },
                confirmButton = {
                    TextButton(onClick = { loginFailed = false }) { Text("确定") }
                }
            )
        }
    }
}

@Composable
fun AccountListSheetContent(
    accounts: List<FtpAccount>,
    onAccountSelected: (FtpAccount) -> Unit,
    onEditClick: (FtpAccount) -> Unit,
    onDeleteClick: (FtpAccount) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 40.dp) // 留出底部安全距离
    ) {
        Text(
            text = "选择账号",
            modifier = Modifier.padding(16.dp),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        // 使用 LazyColumn 渲染列表
        androidx.compose.foundation.lazy.LazyColumn {
            items(accounts.size) { index ->
                val account = accounts[index]
                AccountItem(
                    account = account,
                    onClick = { onAccountSelected(account) },
                    onEditClick = { onEditClick(account) },
                    onDeleteClick = { onDeleteClick(account) }
                )
                if (index < accounts.size - 1) {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
                }
            }
        }
    }
}

@Composable
fun AccountItem(
    account: FtpAccount,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 头像
        Box(
            modifier = Modifier.size(48.dp).background(Color.LightGray, CircleShape).clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (account.avatarPath != null) {
                AsyncImage(
                    model = account.avatarPath,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(account.alias.take(1))
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(text = account.alias, fontSize = 16.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            Text(text = "${account.userName}@${account.ip}", fontSize = 14.sp, color = Color.Gray, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
        }
        // 右侧编辑和删除按钮
        Row {
            IconButton(onClick = onEditClick) {
                Icon(Icons.Default.Edit, contentDescription = "编辑", tint = Color.Gray)
            }
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Default.Delete, contentDescription = "删除", tint = Color.Red.copy(alpha = 0.8f))
            }
        }
    }
}

@Composable
fun LoginContentEmpty(onAddAccount: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .background(Color(0xFFD1E2F5), CircleShape)
        ) {
            Icon(Icons.Default.PersonAddAlt, contentDescription = null, tint = Color(0xFF38B4FF), modifier = Modifier.align(Alignment.Center).size(64.dp))
        }

        Spacer(modifier = Modifier.height(60.dp))

        // 添加账户按钮
        Button(
            onClick = onAddAccount,
            modifier = Modifier
                .width(220.dp)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38B4FF)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(text = "添加账户", color = Color.White, fontSize = 18.sp)
        }
    }
}

@Composable
fun LoginContentWithHistory(
    account: FtpAccount,
    isLoggingIn: Boolean,
    onLogin: () -> Unit
) {
    val userAlias = account.userName + '@' + account.alias
    val ipPort = account.ip + ':' + account.port.toString()

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // --- 用户信息行 (保持不变) ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp)
        ) {
            Box(
                modifier = Modifier.size(100.dp).background(Color.LightGray, CircleShape).clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (account.avatarPath != null) {
                    AsyncImage(
                        model = account.avatarPath,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // 默认显示首字母或占位图
                    Text(account.alias.take(1))
                }
            }
            Spacer(modifier = Modifier.width(24.dp))
            Column {
                Text(text = userAlias, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = ipPort, fontSize = 16.sp, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(50.dp))


        Button(
            onClick = onLogin,
            enabled = !isLoggingIn,
            modifier = Modifier
                .width(240.dp)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38B4FF)),
            shape = RoundedCornerShape(8.dp)
        ) {
            if (isLoggingIn) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "登录中...", color = Color.White, fontSize = 18.sp)
            } else {
                Text(text = "一键登录", color = Color.White, fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun BottomIconAction(label: String, icon: ImageVector, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(Color(0xFFD1E2F5), CircleShape)
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF38B4FF), modifier = Modifier.align(Alignment.Center))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, fontSize = 14.sp)
    }
}