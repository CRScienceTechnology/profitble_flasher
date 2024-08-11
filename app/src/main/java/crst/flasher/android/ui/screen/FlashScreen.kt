package crst.flasher.android.ui.screen   // 标定包路径

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import crst.flasher.android.MainActivityUIState
import crst.flasher.android.MainActivityViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
// 一块物理屏幕的多个物理分区可以由多个Ui kt程序构成
//
fun FlashScreen(uiState: MainActivityUIState, viewModel: MainActivityViewModel) {
    val scope = rememberCoroutineScope()

    Column(  // 划分物理屏幕区域
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = {
                scope.launch {                                // 新知识:协程 https://kimi.moonshot.cn/share/cqrpti2lve9ofug6i93g
                    viewModel.uploadSourceCode(uiState.code)
                }
            }) {
                Text(text = "上传编译")
            }
            Button(onClick = { scope.launch { viewModel.downloadSourceCode() } }) {
                Text(text = "编译下载")
            }
            Button(onClick = { viewModel.flash() }) {
                Text(text = "串口烧录")
            }
        }
    }
}