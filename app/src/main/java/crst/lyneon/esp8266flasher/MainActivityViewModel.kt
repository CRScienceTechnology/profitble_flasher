package crst.lyneon.esp8266flasher // 定义包结构
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody

object Secret {
    const val COMPILE_SERVER_KEY = ""
    const val COMPILE_SERVER = "https://xie.wjwcj.cn/COMPILE_SERVER:3001"
}
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

    fun uploadSourceCode(sourceCode: String) {
        viewModelScope.launch(Dispatchers.IO)
        {
            // 将该字符变量转化成请求体对象
            Log.d("View接收到的数据",sourceCode)
            // 定义一个json数据类

            // TODO: 完成字符串到json的转换，json结构如下
            //{
            //    "name":文件名
            //    "code":代码主体
            //    "filetype":.c文件还是.h文件说明
            //}
            data class SourceCodeRequest(
                val code: String
            )

            val sourceCodeRequest = SourceCodeRequest(code = String.toString())
            val requestBody = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),

            )
            // TODO：成功发送JSON字符串
            OkHttpClient().newBuilder().hostnameVerifier { _, _ -> true }.build().newCall(
                Request.Builder()
                    .url(Secret.COMPILE_SERVER)
                    .post(_sourceCode)
                    .build()
            ).execute()
        }
    }

    fun downloadSourceCode() {
        viewModelScope.launch(Dispatchers.IO) {
            val response = OkHttpClient().newCall(
                Request.Builder()
                    .url("") // TODO: 补全url
                    .get()
                    .build()
            ).execute()
            if (response.isSuccessful) {
                setCode(response.body?.string().toString())
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
    val expandOptionMenu: Boolean = false
)

// 打开c文件并且保存至变量
// 从mainActivityViewModel中获取变量
// 将字符串变量以json文件的方式发送给服务器