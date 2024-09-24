package crst.flasher.android.ui.screen

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import crst.flasher.android.BaseApplication
import crst.flasher.android.MainActivityUIState
import crst.flasher.android.MainActivityViewModel
import crst.flasher.android.util.getFileName
import crst.flasher.android.util.readText
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(
    uiState: MainActivityUIState,
    viewModel: MainActivityViewModel,
    launcher: ActivityResultLauncher<Intent>
) {
    var showReloadDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // 检查是否有之前已打开的文件的记录
        val cacheOpenedFilesFile =
            File(BaseApplication.context.externalCacheDir, "opened_files.txt")
        if (cacheOpenedFilesFile.exists() && uiState.files.isEmpty()) {
            showReloadDialog = true
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (uiState.files.isEmpty()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "没有打开的文件")
                Button(onClick = {
                    Toast.makeText(
                        BaseApplication.context,
                        "选择一个文件",
                        Toast.LENGTH_SHORT
                    ) // 弹出Toast提示
                        .show()
                    val intent =
                        Intent(Intent.ACTION_OPEN_DOCUMENT).apply {             // 创建INTENT对象并设置action类型
                            type =
                                "*/*"                                                   // 设置MIME类型，这里是任意类型
                            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                        }
                    launcher.launch(intent)                                            // 加载inten对象到launcher对象中
                }) {
                    Text(text = "打开文件")
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                PrimaryTabRow(
                    selectedTabIndex = uiState.selectedFileIndex
                ) {
                    uiState.files.fastForEachIndexed { index, uri ->
                        Tab(
                            selected = uiState.selectedFileIndex == index,
                            onClick = {
                                viewModel.setSelectedFileIndex(index)
                                viewModel.setCode(uiState.files[index].readText() ?: "")
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    modifier = Modifier.padding(8.dp),
                                    text = uri.getFileName().toString()
                                )
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    modifier = Modifier.clickable { viewModel.removeFile(uri) }
                                )
                            }
                        }

                    }
                }
                TextField(
                    modifier = Modifier.fillMaxSize(),
                    value = uiState.code,
                    onValueChange = { viewModel.setCode(it) },
                    placeholder = { Text(text = "// Code here") }
                )
            }
        }
    }

    if (showReloadDialog) {
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {
                TextButton(onClick = {
                    showReloadDialog = false
                    val cacheOpenedFilesFile =
                        File(BaseApplication.context.externalCacheDir, "opened_files.txt")
                    cacheOpenedFilesFile.forEachLine {
                        viewModel.addFile(Uri.parse(it))
                    }
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showReloadDialog = false
                    val cacheOpenedFilesFile =
                        File(BaseApplication.context.externalCacheDir, "opened_files.txt")
                    cacheOpenedFilesFile.delete()
                }) { Text("取消") }
            },
            icon = { Icon(imageVector = Icons.Default.Refresh, contentDescription = null) },
            title = { Text(text = "重新加载") },
            text = { Text(text = "发现了上次关闭应用前打开的文件，你想重新加载这些文件吗？") }
        )
    }
}