package crst.flasher.android.util

import android.net.Uri
import android.provider.OpenableColumns
import crst.flasher.android.BaseApplication


fun Uri.readText() =
    BaseApplication.context.contentResolver.openInputStream(this)?.bufferedReader()
        .use { it?.readText() }

fun Uri.writeText(text: String) =
    BaseApplication.context.contentResolver.openOutputStream(this,"wt")?.bufferedWriter()
        .use { it?.write(text) }

fun Uri.getFileName(): String? {
    var result: String? = null
    BaseApplication.context.contentResolver.query(this, null, null, null, null).use { cursor ->
        if (cursor != null && cursor.moveToFirst()) {
            result = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
        }
    }
    return result
}