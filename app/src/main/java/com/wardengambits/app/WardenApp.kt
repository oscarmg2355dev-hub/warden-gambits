package com.wardengambits.app

import android.app.Application
import com.wardengambits.app.ui.map.WorldCache
import com.wardengambits.app.ui.map.WorldDiskCache
import java.io.File

class WardenApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        WorldCache.useDisk(WorldDiskCache(File(cacheDir, "world")))
    }

    override fun onTerminate() {
        container.soundPlayer.release()
        container.musicController.release()
        super.onTerminate()
    }
}
