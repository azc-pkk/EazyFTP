package uestc.b3dman.ftpclient.ui.screens.history

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import uestc.b3dman.ftpclient.data.model.DownloadStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    accountId: Int,
    onBack: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val history by viewModel.history.collectAsState()

    val retryEntry by viewModel.retryTargetEntry.collectAsState()
    if (retryEntry != null) {
        AlertDialog(
            onDismissRequest = { viewModel.onRetryDismiss() },
            title = { Text("重新下载") },
            text = { Text("确定要重新下载 ${retryEntry!!.fileName} 吗？") },
            confirmButton = {
                TextButton(onClick = { viewModel.onRetryConfirm() }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onRetryDismiss() }) { Text("取消") }
            }
        )
    }

    LaunchedEffect(accountId) {
        viewModel.setAccount(accountId)
    }

    BackHandler(enabled = true) {
        onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Download History", fontSize = 24.sp) },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBackIos, "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (history.isEmpty()) {
            Box(
                modifier = Modifier.padding(padding).fillMaxSize().background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text("暂无下载记录", fontSize = 16.sp, color = Color.Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).fillMaxSize().background(Color.White)) {
                items(history) { item ->
                    HistoryEntry(
                        entry = item,
                        onClick = { viewModel.onClickEntry(item) }
                    )
                    HorizontalDivider(thickness = 0.5.dp)
                }
            }
        }
    }
}

@Composable
fun HistoryEntry(
    entry: DownloadHistoryEntryUiState,
    onClick: () -> Unit,
) {
    val (bgColor, icon, iconColor) = when (entry.status) {
        DownloadStatus.SUCCESS -> Triple(Color.White, Icons.Default.CheckCircle, Color(0xFF4CAF50))
        DownloadStatus.FAILED -> Triple(Color(0x15F44336), Icons.Default.Error, Color(0xFFF44336))
        DownloadStatus.DOWNLOADING -> Triple(Color.White, Icons.Default.HourglassEmpty, Color.Gray)
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onClick() }
            .background(bgColor)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = entry.fileName, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Row {
                Text(text = entry.downloadTime, fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = entry.fileSize, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}
