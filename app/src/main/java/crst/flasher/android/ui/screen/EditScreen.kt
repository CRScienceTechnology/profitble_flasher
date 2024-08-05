package crst.flasher.android.ui.screen

import android.content.Intent
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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import crst.flasher.android.BaseApplication
import crst.flasher.android.MainActivityUIState
import crst.flasher.android.MainActivityViewModel
import crst.flasher.android.util.getFileName
import crst.flasher.android.util.readText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(
    uiState: MainActivityUIState,
    viewModel: MainActivityViewModel,
    launcher: ActivityResultLauncher<Intent>
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (uiState.files.isEmpty()) {
            Column {
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
                    onValueChange = { viewModel.setCode(it) }
                )
            }
        }
    }
}