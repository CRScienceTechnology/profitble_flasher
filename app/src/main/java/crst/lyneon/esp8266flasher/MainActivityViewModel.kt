package crst.lyneon.esp8266flasher // 定义包结构
import androidx.lifecycle.ViewModel
import com.jsdroid.editor.CodePane
import com.jsdroid.editor.CodeText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
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

    fun setCodePane(codePane: CodePane) {
        updateUIState { copy(codePane = codePane) }
    }

    fun setCodeText(codeText: CodeText) {
        updateUIState { copy(codeText = codeText) }
    }

    fun selectDevice(device: String) {
        updateUIState { copy(selectedDevice = device) }
    }

    fun setExpandPortSelectDropdownMenu(expand: Boolean) {
        updateUIState { copy(expandPortSelectDropdownMenu = expand) }
    }

    fun modifyProgram() {
        // TODO
    }

    suspend fun uploadSourceCode() {
        withContext(Dispatchers.IO) {
            OkHttpClient().newCall(
                Request.Builder()
                    .url("") // TODO: 补全url
                    .post(
                        uiState.value.codeText?.text.toString()
                            .toRequestBody("application/octet-stream".toMediaType())
                    )
                    .build()
            ).execute()
        }
    }

    suspend fun downloadSourceCode() {
        withContext(Dispatchers.IO) {
            val response = OkHttpClient().newCall(
                Request.Builder()
                    .url("") // TODO: 补全url
                    .get()
                    .build()
            ).execute()
            if (response.isSuccessful) {
                _uiState.value.codeText?.setText(response.body?.string())
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
    val codePane: CodePane? = null,
    val codeText: CodeText? = null,
)