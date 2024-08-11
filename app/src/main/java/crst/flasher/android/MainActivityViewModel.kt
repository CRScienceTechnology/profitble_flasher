package crst.flasher.android // 定义包结构,承担着管理项目文件的功能这样在调用方法时候可以避免
                             // 像python那样即便在同一个目录下导入大量的模块
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import java.util.concurrent.TimeUnit


object MainActivityViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MainActivityUIState())
    val uiState: StateFlow<MainActivityUIState> get() = _uiState.asStateFlow()

    private fun updateUIState(update: MainActivityUIState.() -> MainActivityUIState) {
        _uiState.value = _uiState.value.update()
    }

    fun setCode(code: String) {
        updateUIState { copy(code = code) }
    }

    fun selectDevice(device: String) {
        updateUIState { copy(selectedDevice = device) }
    }

    fun setExpandPortSelectDropdownMenu(expand: Boolean) {
        updateUIState { copy(expandPortSelectDropdownMenu = expand) }
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
    }

    fun removeFile(fileUri: Uri) {
        updateUIState { copy(files = files.apply { remove(fileUri) }) }
        updateUIState { copy(selectedFileIndex = files.lastIndex) }
    }

    fun removeAllFiles() {
        updateUIState { copy(files = files.apply { clear() }) }
    }

    fun setSelectedFileIndex(index: Int) {
        updateUIState { copy(selectedFileIndex = index) }
    }

    fun uploadSourceCode(sourceCode: String) {
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

            val sourceCodeRequest = Json.encodeToString(  // 传入SourceCodeRequestJSON类对象
                SourceCodeRequestJSON(
                    name = "test",        // 名字只取前半部分
                    code = sourceCode,
                    filetype = "c"        // 类型名字不要加点
                )
            )
            val requestBody = sourceCodeRequest.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
            // 发送JSON字符串，这个连续方法有返回结果你也可以单独执行不用一个变量接收返回结果
            val response: Response = OkHttpClient().newBuilder().hostnameVerifier { _, _ -> true }.build().newCall(
                Request.Builder()                     //https://kimi.moonshot.cn/share/cqrphv3df0j2csjduprg 解释
                    .url(Secret.COMPILE_SERVER)       //被kt的包管理知识点坑了
                    .post(requestBody)                // 新知识:依靠连续方法的调用来执行一连续动作
                    .build()
            ).execute()                               // 发送请求并等待响应

            if (response.isSuccessful) {
                Log.d("上传成功", response.body?.string().toString())
            } else {
                Log.d("上传失败", response.body?.string().toString())
            }



        }
    }

    fun downloadSourceCode() {
        viewModelScope.launch(Dispatchers.IO) {
            val client = OkHttpClient().newBuilder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()

            val response = client.newCall(
                Request.Builder()
                    .url(Secret.COMPILE_SERVER)
                    .get()
                    .build()
            ).execute()

            if (response.isSuccessful)
            {
                Log.d("下载提示", "下载成功")
                Log.d("返回信息",response.body?.string().toString())
                setCode(response.body?.string().toString())

            }else{
                Log.e("下载提示", "下载失败")
                Log.e("返回信息",response.body?.string().toString())
            }
        }
    }

    fun flash() {
        // TODO
    }
}

data class MainActivityUIState(
    val selectedDevice: String = "未选中端口",
    val expandPortSelectDropdownMenu: Boolean = false,
    val code: String = "// Source code here",
    val baudRate: String = "",
    val expandOptionMenu: Boolean = false,
    val currentScreen: MainActivity.Screen = MainActivity.Screen.Flash,
    val files: MutableList<Uri> = mutableListOf(),
    val selectedFileIndex: Int = -1
)

// 打开c文件并且保存至变量
// 从mainActivityViewModel中获取变量
// 将字符串变量以json文件的方式发送给服务器