package uestc.b3dman.ftpclient.ui.screens.browser

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserScreen(
    onExit: () -> Unit,
    viewModel: BrowserViewModel = hiltViewModel()
) {
    // 路径
    val pathStack by viewModel.pathStack.collectAsState()
    val currentFolderName = pathStack.last()

    val fileList by viewModel.files.collectAsState()

    // 控制底部菜单显示
    var showSheet by remember { mutableStateOf(false) }
    var selectedFile by remember { mutableStateOf<FtpFileUiState?>(null) }
    val sheetState = rememberModalBottomSheetState()

    // 处理系统返回键
    BackHandler {
        viewModel.onBack(onExit)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentFolderName, fontSize = 24.sp) },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.onBack(onExit)
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBackIos, "Back")
                    }
                },
                actions = {
                    // TODO: 这里的搜索和更多功能暂时不实现，点击后可以弹出提示或者直接无反应
                    IconButton(onClick = { }) { Icon(Icons.Default.Search, "Search") }
                    IconButton(onClick = { }) { Icon(Icons.Default.MoreVert, "More") }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().background(Color.White)) {
            ControlBar()

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(fileList) { file ->
                    FileListItem(
                        file = file,
                        onClick = {
                            if (file.isFolder) {
                                // 文件夹：进入下一级
                                viewModel.onEnter(file.name)
                            } else {
                                // 文件：显示操作菜单
                                selectedFile = file
                                showSheet = true
                            }
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 72.dp), thickness = 0.5.dp)
                }
            }
        }

        // --- 底部操作菜单 ---
        if (showSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSheet = false },
                sheetState = sheetState,
                containerColor = Color.White
            ) {
                FileActionMenu(
                    fileName = selectedFile?.name ?: "",
                    onActionClick = { action ->
                        showSheet = false
                        viewModel.onAction(
                            action,
                            selectedFile
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun FileActionMenu(fileName: String, onActionClick: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        // 顶部显示文件名
        Text(
            text = fileName,
            modifier = Modifier.padding(16.dp),
            fontSize = 14.sp,
            color = Color.Gray
        )

        // 功能列表
        ActionItem(Icons.Default.Download, "下载", onClick = { onActionClick("Download") })
        ActionItem(Icons.Default.Edit, "重命名", onClick = { onActionClick("Rename") })
        ActionItem(Icons.Default.Delete, "删除", color = Color.Red, onClick = { onActionClick("Delete") })
        ActionItem(Icons.Default.Share, "分享", onClick = { onActionClick("Share") })
    }
}

@Composable
fun ActionItem(icon: ImageVector, label: String, color: Color = Color.Black, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = label, fontSize = 16.sp, color = color)
    }
}

// 修改后的 FileListItem 接收点击回调
@Composable
fun FileListItem(file: FtpFileUiState, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() } // 统一触发 onClick
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (file.isFolder) Icons.Default.Folder else Icons.Default.Description,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = Color.Black
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = file.name, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Row {
                Text(text = file.lastUpdateTime, fontSize = 12.sp, color = Color.Gray)
                if (!file.isFolder) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = file.size, fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun ControlBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        // 让左边的文字和右边的图标分别靠在两头
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- 左侧：排序切换 ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { /* 弹出排序选择菜单 */ }
        ) {
            Text(text = "按名称", fontSize = 16.sp, color = Color.Black)
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        }

        // --- 右侧：快捷操作按钮 ---
        Row(verticalAlignment = Alignment.CenterVertically) {
            // 上传按钮
            IconButton(onClick = { /* 触发文件选择器 */ }) {
                Icon(
                    imageVector = Icons.Default.FileUpload,
                    contentDescription = "上传文件",
                    modifier = Modifier.size(26.dp)
                )
            }
            // 新建文件夹按钮
            IconButton(onClick = { /* 弹出输入框新建文件夹 */ }) {
                Icon(
                    imageVector = Icons.Default.CreateNewFolder,
                    contentDescription = "新建文件夹",
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}