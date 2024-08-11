package crst.flasher.android.util

import android.net.Uri                          // 导入uri类，为后文在其基础上自定义方法埋下伏笔
import android.provider.OpenableColumns         //
import crst.flasher.android.BaseApplication     //

                                           // 定义方法:对Uri类变量指向的实际对象读取文件内容,左侧为自定义扩展函数(方法)，右侧为表达式函数
fun Uri.readText() =
    BaseApplication.context.contentResolver.openInputStream(this)?.bufferedReader()
        .use { it?.readText() }            // .bufferedReader()基于输入流创建缓冲区
                                           // .use() 自动关闭资源方法，it指代bufferedReader的返回对象
                                           //  若it?.readText()为空，则返回null，则不执行关闭流动作
//                                              并不是这样。无论lambda中是否发生异常，use函数都会在最后关闭资源

                                           // val uri: Uri =  获取一个 Uri 对象，例如从 ContentProvider 或文件系统
                                           // val text: String? = uri.readText()  调用 readText() 方法

                                           // 定义方法:对Uri类变量指向的实际对象写入文件内容,左侧为自定义扩展函数(方法)，右侧为表达式函数
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