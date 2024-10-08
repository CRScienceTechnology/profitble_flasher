package crst.flasher.android

import android.app.Application

class BaseApplication : Application() {
    companion object {
        lateinit var context: BaseApplication   // 全局应用级上下文，解决你在ui层之外拿不到上下文的烦恼

        fun globalSharedPreference() =
            context.getSharedPreferences("global_shared_preference", MODE_PRIVATE)
    }

    override fun onCreate() {
        super.onCreate()
        context = this
    }
}