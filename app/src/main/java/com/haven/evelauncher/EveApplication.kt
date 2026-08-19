package com.haven.evelauncher

import android.app.Application
import android.content.ComponentCallbacks2

class EveApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Premium initialization: Pre-warm resources if needed
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= ComponentCallbacks2.TRIM_MEMORY_MODERATE) {
            // Launcher should be very careful with memory to stay alive
            // We can clear some non-essential caches here if they grow too large
        }
    }
}
