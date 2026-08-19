package com.haven.evelauncher.data.repository

import android.content.Context
import com.haven.evelauncher.core.widget.*
import com.haven.evelauncher.core.context.EveContextEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale

class EveDataRepository(
    private val context: Context,
    private val engine: EveContextEngine
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val locationRepo = LocationRepository(context)
    private val weatherRepo = WeatherRepository()
    private val healthRepo = HealthRepository(context)

    fun startSync() {
        // Precise Location & Reactive Weather
        scope.launch {
            try {
                locationRepo.getLocationUpdates()
                    .filterNotNull()
                    .distinctUntilChanged { old, new -> 
                        Math.abs(old.latitude - new.latitude) < 0.01 && Math.abs(old.longitude - new.longitude) < 0.01
                    }
                    .collectLatest { location ->
                        refreshWeather(location.latitude, location.longitude)
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Real Step Data Sync
        scope.launch {
            try {
                healthRepo.getTodaySteps()
                    .catch { e -> e.printStackTrace(); emit(0) }
                    .collect { steps ->
                        // This repo still uses the old candidate system if called.
                        // But the new WidgetEngine is preferred.
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun refreshWeather(lat: Double, lon: Double) {
        // Real fetching handled by WeatherDataSource now.
    }
}
