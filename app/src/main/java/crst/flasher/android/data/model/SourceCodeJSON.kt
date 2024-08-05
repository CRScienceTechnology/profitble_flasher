package crst.flasher.android.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SourceCodeRequestJSON(
    val name: String,
    val code: String,
    val filetype: String
)
