package uestc.b3dman.ftpclient.ui.screens.history

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    accountId: Int,
    onBack: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val history by viewModel.history.collectAsState()

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
                },
                actions = {
                    // TODO: 下载目录之类的设置
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize().background(Color.White)) {
            items(history) { item ->
                HistoryEntry(item)
                HorizontalDivider(thickness = 0.5.dp)
            }
        }
    }
}

@Composable
fun HistoryEntry(
    entry: DownloadHistoryEntryUiState,
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .clickable{ /* TODO */ }
            .padding(16.dp)
            // TODO: 根据状态更改背景颜色
    ) {
        Icon(
            // TODO: 根据状态更改图标
            imageVector = Icons.Default.Download,
            contentDescription = null,
            tint = Color(0xFF4CAF50), // 绿色图标
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