package crst.lyneon.esp8266flasher // 定义包结构
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

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

    fun uploadSourceCode() {
        viewModelScope.launch(Dispatchers.IO) {
            OkHttpClient().newCall(
                Request.Builder()
                    .url("https://182.92.143.75/COMPILE_SERVER:11451")
                    .post(uiState.value.code.toRequestBody("application/octet-stream".toMediaType()))
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