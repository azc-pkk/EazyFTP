package uestc.b3dman.ftpclient.ui.screens.addaccount

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccountScreen(
    onBack: () -> Unit,
    viewModel: AddAccountViewModel = hiltViewModel()
) {
    // 定义各个输入框的状态
    var ip by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var alias by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        // 当用户选完图片后，回调会返回图片的 URI
        selectedImageUri = uri
    }

    val fieldColor = Color(0xFFE0E0E0) // 浅灰色输入框背景

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. 顶部返回按钮
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

        // 2. 标题
        Text(
            text = "Easy FTP",
            fontSize = 32.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(40.dp))

        // 3. 上传头像圆圈
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
            if (selectedImageUri != null) {
                // 使用 Coil 加载选中的图片
                AsyncImage(
                    model = selectedImageUri,
                    contentDescription = "Avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop // 填满圆圈
                )
            } else {
                Text(text = "上传头像", color = Color.Black, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(50.dp))

        // 4. 输入框表单
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CustomInputField(value = ip, onValueChange = { ip = it }, placeholder = "服务端 IP ( 端口默认为21 )")
            CustomInputField(value = username, onValueChange = { username = it }, placeholder = "用户名 ( 匿名登录不填 )")
            CustomInputField(value = password, onValueChange = { password = it }, placeholder = "密码 ( 可为空 )")
            CustomInputField(value = alias, onValueChange = { alias = it }, placeholder = "服务器备注 ( 可不填 )")
        }

        Spacer(modifier = Modifier.weight(1f))

        // 5. 确认按钮
        Button(
            onClick = {
                val host = ip.split(':', limit = 2)[0]
                val port = ip.split(':', limit = 2).getOrNull(1)?.toIntOrNull() ?: 21
                viewModel.addAccount(
                    ip = host,
                    port = port,
                    userName = username.ifBlank { "anonymous" },
                    password = password,
                    alias = alias.ifBlank { ip },
                    avatarUri = selectedImageUri,
                    onSuccess = onBack,
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
fun CustomInputField(value: String, onValueChange: (String) -> Unit, placeholder: String) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = Color.DarkGray) },
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .height(60.dp),
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