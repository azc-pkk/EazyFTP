package uestc.b3dman.ftpclient.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import uestc.b3dman.ftpclient.data.model.FtpAccount

// 定义主题蓝色
val FtpBlue = Color(56, 182, 255)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigateToBrowser: () -> Unit,
    onNavigateToAddAccount: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
//    // TODO: 这里的 hasHistory 只是为了演示两种 UI 状态，实际应该根据用户数据来决定
//    // 模拟是否有历史记录的状态，点击顶部的 "Easy FTP" 可以切换状态观察 UI
//    var hasHistory by remember { mutableStateOf(false) }
    val accounts by viewModel.accounts.collectAsState(initial = emptyList())
    val isLoggingIn by viewModel.isLoggingIn.collectAsState()

    // 控制底部弹窗显示
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

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

        val recentAccount = accounts.maxByOrNull { it.lastLoginTime }
        if (recentAccount == null) {
            // --- 情况 1: 展现 1.png 的内容 ---
            LoginContentEmpty(onAddAccount = onNavigateToAddAccount)
        } else {
            // --- 情况 2: 展现 2.png 的内容 ---
            // 将 accounts 按 lastLoginTime 排序，取最近登录的一个账号展示
            LoginContentWithHistory(
                account = recentAccount,
                onLoginSuccess = onNavigateToBrowser
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
                BottomIconAction(label = "添加账号", onClick = onNavigateToAddAccount)
                BottomIconAction(label = "切换账号", onClick = { showSheet = true })
            }
        }

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
                    }
                )
            }
        }
    }
}

@Composable
fun AccountListSheetContent(
    accounts: List<FtpAccount>,
    onAccountSelected: (FtpAccount) -> Unit
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
                AccountItem(account = account, onClick = { onAccountSelected(account) })
                if (index < accounts.size - 1) {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
                }
            }
        }
    }
}

@Composable
fun AccountItem(account: FtpAccount, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 账号头像占位
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
                // 默认显示首字母或占位图
                Text(account.alias.take(1))
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = account.alias, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(text = "${account.userName}@${account.ip}", fontSize = 14.sp, color = Color.Gray)
        }
    }
}

@Composable
fun LoginContentEmpty(onAddAccount: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // TODO: 这里的黑色圆块只是占位，实际应该替换成一个更有设计感的图标或者插图
        // 大圆黑块
        Box(
            modifier = Modifier
                .size(160.dp)
                .background(Color.Black, CircleShape)
        )

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
    onLoginSuccess: () -> Unit // 登录成功回调，传出密码或触发跳转
) {
    val userAlias = account.userName + '@' + account.alias
    val ipPort = account.ip + ':' + account.port.toString()
    // 状态 1: 是否处于密码输入模式
    var isPasswordMode by remember { mutableStateOf(false) }
    // 状态 2: 密码输入框的内容
    var password by remember { mutableStateOf("") }

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

        Spacer(modifier = Modifier.height(40.dp))

        // --- 动态切换区域 ---
        if (!isPasswordMode) {
            // 模式 A: 初始状态 (一键登录)
            Button(
                onClick = { onLoginSuccess() },
                modifier = Modifier
                    .width(240.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38B4FF)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "一键登录", color = Color.White, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { isPasswordMode = true }) {
                Text(text = "密码登录", color = Color.Black, fontSize = 16.sp)
            }
        } else {
            // 模式 B: 密码输入状态
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 密码输入框 (复用之前定义的 CustomInputField 样式)
                // 这里我们稍微修改一下，增加 passwordVisualTransformation 隐藏明文
                PasswordInputField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "请输入密码"
                )

                // 确定按钮
                Button(
                    onClick = { onLoginSuccess() },
                    modifier = Modifier
                        .width(240.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38B4FF)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = "确定", color = Color.White, fontSize = 18.sp)
                }

                // 取消按钮（可选，方便用户切回一键登录）
                TextButton(onClick = { isPasswordMode = false }) {
                    Text(text = "取消", color = Color.Gray, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun BottomIconAction(label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(Color.Black, CircleShape)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, fontSize = 14.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordInputField(value: String, onValueChange: (String) -> Unit, placeholder: String) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = Color.Gray) },
        modifier = Modifier
            .width(280.dp) // 宽度稍微宽于按钮
            .height(60.dp),
        visualTransformation = PasswordVisualTransformation(), // 关键：将文本转为点点
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFE0E0E0),
            unfocusedContainerColor = Color(0xFFE0E0E0),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = Color.Black
        ),
        shape = RoundedCornerShape(8.dp),
        singleLine = true
    )
}