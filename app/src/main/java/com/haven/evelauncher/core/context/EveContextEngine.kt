package com.haven.evelauncher.core.context

import android.content.Context
import com.haven.evelauncher.platform.apps.AppInfo
import com.haven.evelauncher.core.widget.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar

/**
 * Eve Context Engine: Bridging legacy orbit data with new widget engine.
 */
class EveContextEngine(val context: Context) {
    private val _currentContext = MutableStateFlow<EveContext>(EveContext())
    val currentContext: StateFlow<EveContext> = _currentContext.asStateFlow()

    fun updateOrbit(apps: List<AppInfo>) {
        val currentTime = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        val timeOfDay = deriveTimeOfDay(calendar)
        
        val scoredApps = apps.map { app ->
            var score = 0
            
            // 1. Recency Score (High impact)
            val minutesSinceUse = (currentTime - app.lastUsed) / 60000L
            if (minutesSinceUse < 10L) score += 100 // Very recent
            else if (minutesSinceUse < 60L) score += 60
            else if (minutesSinceUse < 1440L) score += 20 // Last 24h
            
            // 2. Favorite Bonus
            if (app.isFavorite) score += 80
            
            // 3. Contextual Bonus
            when (timeOfDay) {
                TimeOfDay.MORNING -> if (app.packageName.contains("calendar") || app.packageName.contains("news")) score += 40
                TimeOfDay.EVENING -> if (app.packageName.contains("spotify") || app.packageName.contains("youtube")) score += 45
                else -> {}
            }
            
            val coreApps = listOf("com.android.chrome", "com.google.android.apps.messaging", "com.whatsapp")
            if (coreApps.contains(app.packageName)) score += 30
            
            app to score
        }.sortedByDescending { it.second }.map { it.first }.take(9)

        _currentContext.value = _currentContext.value.copy(
            orbit = OrbitData(predictiveApps = scoredApps)
        )
    }

    private fun deriveTimeOfDay(calendar: Calendar): TimeOfDay {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 0..4 -> TimeOfDay.NIGHT
            in 5..7 -> TimeOfDay.EARLY_MORNING
            in 8..11 -> TimeOfDay.MORNING
            in 12..16 -> TimeOfDay.AFTERNOON
            in 17..20 -> TimeOfDay.EVENING
            else -> TimeOfDay.LATE_NIGHT
        }
    }

    fun updateGreeting(greeting: String) {
        _currentContext.value = _currentContext.value.copy(greeting = greeting)
    }
    
    fun registerCandidate(candidate: Any) {}
}
