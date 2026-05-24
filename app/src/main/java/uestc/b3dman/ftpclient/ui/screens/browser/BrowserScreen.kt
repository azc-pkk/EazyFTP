package uestc.b3dman.ftpclient.ui.screens.browser

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserScreen(
    accountId: Int,
    onExit: () -> Unit,
    onNavigateToHistory: (Int) -> Unit,
    viewModel: BrowserViewModel = hiltViewModel()
) {
    val pathStack by viewModel.pathStack.collectAsState()
    val currentFolderName = pathStack.last()

    val fileList by viewModel.files.collectAsState()

    var showSheet by remember { mutableStateOf(false) }
    var selectedFile by remember { mutableStateOf<FtpFileUiState?>(null) }
    val sheetState = rememberModalBottomSheetState()

    val pickFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        uri?.let { viewModel.uploadFile(it) }
    }

    LaunchedEffect(accountId) {
        viewModel.accountId = accountId
    }

    val isSearchActive by viewModel.isSearchActive.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    BackHandler {
        if (isSearchActive) {
            viewModel.toggleSearch()
        } else {
            viewModel.onBack(onExit)
        }
    }

    Scaffold(
        topBar = {
            if (isSearchActive) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { viewModel.updateSearchQuery(it) },
                    onClose = { viewModel.toggleSearch() }
                )
            } else {
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
                        IconButton(onClick = { onNavigateToHistory(accountId) }) { Icon(Icons.Default.Download, "Download History") }
                        IconButton(onClick = { viewModel.toggleSearch() }) { Icon(Icons.Default.Search, "Search") }
                        IconButton(onClick = { }) { Icon(Icons.Default.MoreVert, "More") }
                    }
                )
            }
        }
    ) { padding ->
        val currentSortType by viewModel.currentSortType.collectAsState()

        Column(modifier = Modifier.padding(padding).fillMaxSize().background(Color.White)) {
            ControlBar(
                currentSortType = currentSortType,
                onSortTypeChange = { viewModel.setSortType(it) },
                onUploadClick = { pickFileLauncher.launch("*/*") },
                onCreateFolderClick = { viewModel.onCreateFolderClick() }
            )

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(fileList) { file ->
                    FileListItem(
                        file = file,
                        onClick = {
                            if (file.isFolder) {
                                viewModel.onEnter(file.name)
                            } else {
                                selectedFile = file
                                showSheet = true
                            }
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 72.dp), thickness = 0.5.dp)
                }
            }
        }

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
                        viewModel.onAction(action, selectedFile)
                    }
                )
            }
        }

        val renameFile by viewModel.renameTargetFile.collectAsState()
        if (renameFile != null) {
            RenameDialog(
                currentName = renameFile!!.name,
                isFolder = renameFile!!.isFolder,
                onConfirm = { newName -> viewModel.onRenameConfirm(newName) },
                onDismiss = { viewModel.onRenameDismiss() }
            )
        }

        val showCreateFolder by viewModel.showCreateFolderDialog.collectAsState()
        if (showCreateFolder) {
            CreateFolderDialog(
                onConfirm = { name -> viewModel.onCreateFolderConfirm(name) },
                onDismiss = { viewModel.onCreateFolderDismiss() }
            )
        }

        val deleteFile by viewModel.deleteTargetFile.collectAsState()
        if (deleteFile != null) {
            DeleteConfirmDialog(
                fileName = deleteFile!!.name,
                onConfirm = { viewModel.onDeleteConfirm() },
                onDismiss = { viewModel.onDeleteDismiss() }
            )
        }
    }
}

@Composable
fun CreateFolderDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var folderName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新建文件夹") },
        text = {
            OutlinedTextField(
                value = folderName,
                onValueChange = { folderName = it },
                label = { Text("文件夹名称") },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done)
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(folderName) },
                enabled = folderName.isNotBlank()
            ) { Text("确定") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit
) {
    TopAppBar(
        title = {
            TextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("搜索文件名...", color = Color.Gray) },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )
        },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(Icons.AutoMirrored.Filled.ArrowBackIos, contentDescription = "关闭搜索")
            }
        },
        actions = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "清除")
                }
            }
        }
    )
}

@Composable
fun DeleteConfirmDialog(
    fileName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("确认删除") },
        text = { Text("确定要删除 $fileName 吗？此操作不可撤销。") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("删除", color = Color.Red)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

@Composable
fun RenameDialog(
    currentName: String,
    isFolder: Boolean,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var newName by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isFolder) "重命名文件夹" else "重命名文件") },
        text = {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("新名称") },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done)
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(newName) },
                enabled = newName.isNotBlank() && newName != currentName
            ) { Text("确定") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

@Composable
fun FileActionMenu(fileName: String, onActionClick: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = fileName,
            modifier = Modifier.padding(16.dp),
            fontSize = 14.sp,
            color = Color.Gray
        )

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

@Composable
fun FileListItem(file: FtpFileUiState, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
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
fun ControlBar(
    currentSortType: BrowserViewModel.SortType,
    onSortTypeChange: (BrowserViewModel.SortType) -> Unit,
    onUploadClick: () -> Unit,
    onCreateFolderClick: () -> Unit,
) {
    var showSortMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { showSortMenu = true }
            ) {
                Text(text = currentSortType.displayName, fontSize = 16.sp, color = Color.Black)
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
            DropdownMenu(
                expanded = showSortMenu,
                onDismissRequest = { showSortMenu = false }
            ) {
                BrowserViewModel.SortType.entries.forEach { sortType ->
                    DropdownMenuItem(
                        text = { Text(sortType.displayName) },
                        onClick = {
                            onSortTypeChange(sortType)
                            showSortMenu = false
                        },
                        leadingIcon = {
                            if (sortType == currentSortType) {
                                Icon(Icons.Default.Check, contentDescription = null)
                            }
                        }
                    )
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onUploadClick() }) {
                Icon(
                    imageVector = Icons.Default.FileUpload,
                    contentDescription = "上传文件",
                    modifier = Modifier.size(26.dp)
                )
            }
            IconButton(onClick = { onCreateFolderClick() }) {
                Icon(
                    imageVector = Icons.Default.CreateNewFolder,
                    contentDescription = "新建文件夹",
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}
