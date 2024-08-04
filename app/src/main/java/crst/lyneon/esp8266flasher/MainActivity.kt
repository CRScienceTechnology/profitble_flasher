package crst.lyneon.esp8266flasher

import android.content.Context.USB_SERVICE
import android.content.Intent
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hoho.android.usbserial.driver.UsbSerialProber
import crst.lyneon.esp8266flasher.ui.theme.ESP8266FlasherTheme
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel = viewModel<MainActivityViewModel>()
            val uiState by viewModel.uiState.collectAsState()

            ESP8266FlasherTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = { Text(text = stringResource(R.string.app_name)) },
                            actions = {
                                IconButton(onClick = { viewModel.setExpandOptionMenu(true) }) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = null
                                    )
                                }
                                DropdownMenu(
                                    expanded = uiState.expandOptionMenu,
                                    onDismissRequest = { viewModel.setExpandOptionMenu(false) }
                                ) {
                                    Text(text = "配置", modifier = Modifier.padding(8.dp))
                                    TextField(
                                        value = uiState.baudRate,
                                        onValueChange = { viewModel.setBaudRate(it) },
                                        label = {
                                            Text(text = "波特率")
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            PortSelectRow(uiState = uiState, viewModel = viewModel)
                                        },
                                        onClick = { }
                                    )
                                }
                            }
                        )
                    },
                ) { innerPadding ->
                    val mainActivity = LocalContext.current as MainActivity
                    val scope = rememberCoroutineScope()
                    val launcher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.StartActivityForResult()
                    ) { result ->
                        if (result.resultCode == RESULT_OK) {
                            result.data?.data?.let { uri ->
                                val contentResolver = mainActivity.contentResolver
                                contentResolver.openInputStream(uri)?.bufferedReader()?.use {
                                    viewModel.setCode(it.readText())
                                }
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                    ) {
                        TextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(horizontal = 16.dp),
                            value = uiState.code,
                            onValueChange = { viewModel.setCode(it) }
                        )
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(onClick = {
                                    Toast.makeText(mainActivity, "选择一个文件", Toast.LENGTH_SHORT) // 弹出Toast提示
                                        .show()
                                    val intent = Intent(Intent.ACTION_GET_CONTENT).apply {   // 创建INTENT对象并设置action类型
                                        type = "*/*"                                         // 设置MIME类型，这里是任意类型
                                    }
                                    launcher.launch(intent)
                                }) {
                                    Text(text = "加载程序")
                                }
                                Button(onClick = { scope.launch { viewModel.uploadSourceCode() } }) {
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
                }
            }
        }
    }
}

@Composable
private fun PortSelectRow(uiState: MainActivityUIState, viewModel: MainActivityViewModel) {
    val usbManager =
        BaseApplication.context.getSystemService(USB_SERVICE) as UsbManager

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = uiState.selectedDevice)
        IconButton(onClick = {
            viewModel.setExpandPortSelectDropdownMenu(
                true
            )
        }) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null
            )
            DropdownMenu(
                expanded = uiState.expandPortSelectDropdownMenu,
                onDismissRequest = {
                    viewModel.setExpandPortSelectDropdownMenu(false)
                }) {
                val availableDrivers = UsbSerialProber.getDefaultProber()
                    .findAllDrivers(usbManager)

                Text(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    text = "选择端口"
                )
                availableDrivers.forEach { driver ->
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text(text = driver.ports.toString()) },
                        onClick = {
                            // 请求选中的usb设备的权限
                            /*usbManager.requestPermission(
                                it.value,
                                PendingIntent.getBroadcast( mainActivity, 0,  Intent(), PendingIntent.FLAG_IMMUTABLE  )
                            )*/
                            viewModel.selectDevice(driver.ports.toString())
                        }
                    )
                }
            }
        }
    }
}