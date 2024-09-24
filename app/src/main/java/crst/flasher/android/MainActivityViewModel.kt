package crst.flasher.android // 定义包结构,承担着管理项目文件的功能这样在调用方法时候可以避免
// 像python那样即便在同一个目录下导入大量的模块
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbManager
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort
import crst.flasher.android.data.Constant
import crst.flasher.android.data.model.SourceCodeRequestJSON
import crst.flasher.android.util.readText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit


object MainActivityViewModel : ViewModel() {
    //    ViewModel是mvvm架构中重要的一层，用于存储和修改ui相关的状态，有助于ui与逻辑分离
    private val _uiState = MutableStateFlow(MainActivityUIState())
    val uiState: StateFlow<MainActivityUIState> get() = _uiState.asStateFlow()

    private fun updateUIState(update: MainActivityUIState.() -> MainActivityUIState) {
        _uiState.value = _uiState.value.update()
    }

    fun setCode(code: String) {
        updateUIState { copy(code = code) }
    }

    fun selectDevice(device: UsbSerialDriver?) {
        updateUIState { copy(selectedDevice = device) }
    }

    fun setExpandPortSelectList(expand: Boolean) {
        updateUIState { copy(expandPortSelectList = expand) }
    }

    fun setBaudRate(baudRate: String) {
        updateUIState { copy(baudRate = baudRate) }
    }

    fun setExpandOptionMenu(expand: Boolean) {
        updateUIState { copy(expandOptionMenu = expand) }
    }

    fun setCurrentScreen(screen: MainActivity.Screen) {
        updateUIState { copy(currentScreen = screen) }
    }

    fun addFile(fileUri: Uri) {
        updateUIState { copy(files = files.apply { add(fileUri) }) }
        updateUIState { copy(selectedFileIndex = files.indexOf(fileUri)) }
        updateUIState { copy(code = fileUri.readText().toString()) }
        BaseApplication.globalSharedPreference().edit {
            putStringSet(
                Constant.SP_KEY_OPENED_FILES,
                uiState.value.files.map { it.toString() }.toSet()
            )
        }
        saveOpenedFilesUri()
    }

    fun removeFile(fileUri: Uri) {
        updateUIState { copy(files = files.apply { remove(fileUri) }) }
        updateUIState { copy(selectedFileIndex = files.lastIndex) }
        updateUIState {
            if (selectedFileIndex in 0..files.lastIndex) copy(
                code = files[selectedFileIndex].readText() ?: ""
            ) else copy()
        }
        saveOpenedFilesUri()
    }

    fun removeAllFiles() {
        updateUIState { copy(files = files.apply { clear() }) }
        updateUIState { copy(selectedFileIndex = -1) }
        saveOpenedFilesUri()
    }

    // 记录当前已打开的文件的uri到缓存文件中，用于下次启动app时重新加载
    private fun saveOpenedFilesUri() {
        val cacheOpenedFilesFile =
            File(BaseApplication.context.externalCacheDir, "opened_files.txt")
        if (uiState.value.files.isEmpty()) {
            cacheOpenedFilesFile.delete()
        } else {
            val sb = StringBuilder()
            uiState.value.files.forEach { file ->
                sb.appendLine(file.toString())
            }
            cacheOpenedFilesFile.writeText(sb.toString())
        }
    }

    fun setSelectedFileIndex(index: Int) {
        updateUIState { copy(selectedFileIndex = index) }
    }

    fun uploadSourceCode(sourceCode: String) {
        val remoteServerUrl = BaseApplication.globalSharedPreference()
            .getString(Constant.SP_KEY_REMOTE_SERVER_URL, "").toString()
        if (remoteServerUrl.isEmpty()) {
            Toast.makeText(BaseApplication.context, "请先配置远程服务器地址", Toast.LENGTH_SHORT)
                .show()
            return
        }

        viewModelScope.launch(Dispatchers.IO)   // 此函数是放在协程中运行的
        {
            // 将该字符变量转化成请求体对象
            Log.d("从View接收到的数据", sourceCode)
            // 定义一个json数据类

            // 完成字符串到json的转换，json结构如下
            //{
            //    "name":文件名
            //    "code":代码主体
            //    "filetype":.c文件还是.h文件说明
            //}

            try {
                val sourceCodeRequest = Json.encodeToString(  // 传入SourceCodeRequestJSON类对象
                    SourceCodeRequestJSON(
                        name = "test",        // 名字只取前半部分
                        code = sourceCode,
                        filetype = "c"        // 类型名字不要加点
                    )
                )
                val requestBody =
                    sourceCodeRequest.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
                // 发送JSON字符串，这个连续方法有返回结果你也可以单独执行不用一个变量接收返回结果
                val response: Response =
                    OkHttpClient().newBuilder().hostnameVerifier { _, _ -> true }.build().newCall(
                        Request.Builder()                     //https://kimi.moonshot.cn/share/cqrphv3df0j2csjduprg 解释
                            .url(remoteServerUrl)       //被kt的包管理知识点坑了
                            .post(requestBody)                // 新知识:依靠连续方法的调用来执行一连续动作
//                                                          这个是链式调用，每个函数执行完操作后都会返回当前对象的引用，js里也有这种api设计啊
                            .build()
                    ).execute()                               // 发送请求并等待响应

                launch(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Log.d("上传成功", response.body?.string().toString())
                        Toast.makeText(BaseApplication.context, "上传成功", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Log.d("上传失败", response.body?.string().toString())
                        Toast.makeText(BaseApplication.context, "上传失败", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            } catch (e: Exception) {
                Log.e("上传异常", e.message.toString())
                viewModelScope.launch(Dispatchers.Main) {
                    Toast.makeText(BaseApplication.context, "上传异常", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * 尝试从服务器下载hex文件并保存到本地Download文件夹中
     */
    fun downloadHexFile() {
        val remoteServerUrl = BaseApplication.globalSharedPreference()
            .getString(Constant.SP_KEY_REMOTE_SERVER_URL, "").toString()
        if (remoteServerUrl.isEmpty()) {
            Toast.makeText(BaseApplication.context, "请先配置远程服务器地址", Toast.LENGTH_SHORT)
                .show()
            return
        }

        Toast.makeText(BaseApplication.context, "开始下载", Toast.LENGTH_SHORT).show()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val client = OkHttpClient().newBuilder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build()

                val response = client.newCall(
                    Request.Builder()
                        .url(remoteServerUrl)
                        .get()
                        .build()
                ).execute()

                if (response.isSuccessful) {
                    launch(Dispatchers.Main) {
                        Toast.makeText(BaseApplication.context, "下载成功", Toast.LENGTH_SHORT)
                            .show()
                    }
                    Log.d("下载提示", "下载成功")
                    Log.d("返回信息", response.body?.string().toString())
                    response.body?.byteStream().use { inputStream ->
                        val downloadsDir =
                            BaseApplication.context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                                ?: return@use
                        val file =
                            File(downloadsDir, System.currentTimeMillis().toString() + ".hex")
                        try {
                            inputStream?.let {
                                file.outputStream().use { outputStream ->
                                    inputStream.copyTo(outputStream)
                                }
                            }
                            launch(Dispatchers.Main) {
                                Toast.makeText(
                                    BaseApplication.context,
                                    "保存成功",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                            launch(Dispatchers.Main) {
                                Toast.makeText(
                                    BaseApplication.context,
                                    "保存失败",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                } else {
                    val errorMessage = response.body?.string().toString()
                    launch(Dispatchers.Main) {
                        Toast.makeText(
                            BaseApplication.context,
                            "下载失败：${errorMessage}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    Log.e("下载提示", "下载失败")
                    Log.e("返回信息", errorMessage)
                }
            } catch (e: Exception) {
                Log.e("下载异常", e.message.toString())
                launch(Dispatchers.Main) {
                    Toast.makeText(BaseApplication.context, "下载异常", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * 启动烧录流程，此函数会尝试打开用户选中的usb串口设备，成功打开后使用参数中的launcher启动文件选择器让用户选择要烧录的hex文件
     */
    fun startFlash(launcher: ActivityResultLauncher<Intent>) {
        if (uiState.value.selectedDevice == null) {
            Toast.makeText(BaseApplication.context, "请选择设备", Toast.LENGTH_SHORT).show()
            return
        }
        if (uiState.value.baudRate.isEmpty()) {
            Toast.makeText(BaseApplication.context, "请配置波特率", Toast.LENGTH_SHORT).show()
            return
        }
        val usbManager = BaseApplication.context.getSystemService(Context.USB_SERVICE) as UsbManager
        val serialDriver = uiState.value.selectedDevice!!
        val port = serialDriver.ports[0]
        try {
            port.open(usbManager.openDevice(serialDriver.device))
            port.setParameters(
                uiState.value.baudRate.toInt(),
                UsbSerialPort.DATABITS_8,
                UsbSerialPort.STOPBITS_1,
                UsbSerialPort.PARITY_NONE
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                BaseApplication.context,
                "打开串口失败，请尝试重新选择设备或配置波特率",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        Toast.makeText(
            BaseApplication.context,
            "打开串口成功。请选择hex文件进行烧录",
            Toast.LENGTH_SHORT
        ).show()
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "*/*"
        }
        launcher.launch(intent)
    }

    /**
     * 执行烧录流程，此函数会将hex文件内容发送给串口设备进行烧录
     */
    fun executeFlash(hexFileUri: Uri) {
        if (uiState.value.selectedDevice == null) {
            Toast.makeText(BaseApplication.context, "请选择设备", Toast.LENGTH_SHORT).show()
            return
        }
        if (uiState.value.baudRate.isEmpty()) {
            Toast.makeText(BaseApplication.context, "请配置波特率", Toast.LENGTH_SHORT).show()
            return
        }
        val serialDriver = uiState.value.selectedDevice!!
        val port = serialDriver.ports[0]
        try {
            port.write(hexFileUri.readText().toString().toByteArray(), 8000)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(BaseApplication.context, "烧录失败", Toast.LENGTH_SHORT).show()
            return
        }
        Toast.makeText(BaseApplication.context, "烧录成功", Toast.LENGTH_SHORT).show()
    }
}

data class MainActivityUIState(
    val selectedDevice: UsbSerialDriver? = null,
    val expandPortSelectList: Boolean = false,
    val code: String = "",
    val baudRate: String = "9600",
    val expandOptionMenu: Boolean = false,
    val currentScreen: MainActivity.Screen = MainActivity.Screen.Flash,
    val files: MutableList<Uri> = mutableListOf(),
    val selectedFileIndex: Int = -1
)

// 打开c文件并且保存至变量
// 从mainActivityViewModel中获取变量
// 将字符串变量以json文件的方式发送给服务器