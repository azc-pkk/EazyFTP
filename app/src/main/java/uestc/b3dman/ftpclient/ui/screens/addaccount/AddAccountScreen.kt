package uestc.b3dman.ftpclient.ui.screens.addaccount

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccountScreen(
    accountId: Int,
    onBack: () -> Unit,
    viewModel: AddAccountViewModel = hiltViewModel()
) {
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        viewModel.updateAvatarUri(uri)
    }

    val ipAndPort by viewModel.ipAndPort.collectAsState()
    val username by viewModel.username.collectAsState()
    val password by viewModel.password.collectAsState()
    val alias by viewModel.alias.collectAsState()
    val avatarUri by viewModel.avatarUri.collectAsState()

    LaunchedEffect(accountId) {
        viewModel.loadAccount(accountId)
    }

    val validationError by viewModel.validationError.collectAsState()
    if (validationError != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearValidationError() },
            title = { Text("输入错误") },
            text = { Text(validationError!!) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearValidationError() }) { Text("确定") }
            }
        )
    }

    val fieldColor = Color(0xFFE0E0E0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBackIosNew,
                    contentDescription = "返回",
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Easy FTP",
            fontSize = 32.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(40.dp))

        Box(
            modifier = Modifier
                .size(140.dp)
                .background(fieldColor, CircleShape)
                .clickable {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (avatarUri != null) {
                AsyncImage(
                    model = avatarUri,
                    contentDescription = "Avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(text = "上传头像", color = Color.Black, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(50.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CustomInputField(value = ipAndPort, onValueChange = { viewModel.updateIpAndPort(it) }, placeholder = "服务端 IP ( 端口默认为21 )")
            CustomInputField(value = username, onValueChange = { viewModel.updateUsername(it) }, placeholder = "用户名 ( 匿名登录不填 )")
            CustomInputField(value = password, onValueChange = { viewModel.updatePassword(it) }, placeholder = "密码 ( 可为空 )")
            CustomInputField(value = alias, onValueChange = { viewModel.updateAlias(it) }, placeholder = "服务器备注 ( 可不填 )", imeAction = ImeAction.Done)
        }

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = {
                viewModel.addAccount(
                    onSuccess = onBack
                )
            },
            modifier = Modifier
                .width(180.dp)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38B4FF)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(text = "确认", color = Color.White, fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    imeAction: ImeAction = ImeAction.Next
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = Color.DarkGray) },
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .height(60.dp),
        // 设置键盘选项
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = imeAction),
        keyboardActions = KeyboardActions.Default,
        colors = TextFieldDefaults.colors(
            // 设置容器背景颜色
            focusedContainerColor = Color(0xFFE0E0E0),
            unfocusedContainerColor = Color(0xFFE0E0E0),
            disabledContainerColor = Color(0xFFE0E0E0),

            // 将底部的指示线设为透明（隐藏它）
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,

            // 光标颜色
            cursorColor = Color.Black
        ),
        shape = RoundedCornerShape(8.dp),
        singleLine = true
    )
}