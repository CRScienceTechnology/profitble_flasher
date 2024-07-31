package crst.lyneon.esp8266flasher

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