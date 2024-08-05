package crst.flasher.android

import android.app.Application

class BaseApplication : Application() {
    companion object {
        lateinit var context: BaseApplication
    }

    override fun onCreate() {
        super.onCreate()
        context = this
    }
}