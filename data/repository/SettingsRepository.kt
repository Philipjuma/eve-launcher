package com.haven.evelauncher.data.repository

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class ClockSize { SMALL, MEDIUM, LARGE }
enum class BlurIntensity { SUBTLE, BALANCED, STRONG }

data class EveSettings(
    val clockSize: ClockSize = ClockSize.LARGE,
    val isClockDynamic: Boolean = true,
    val personalClockColor: Int = 0xFFFFFFFF.toInt(),
    val blurIntensity: BlurIntensity = BlurIntensity.BALANCED,
    val animationsEnabled: Boolean = true,
    val appDrawerColumns: Int = 4,
    val appDrawerRows: Int = 5
)

class SettingsRepository(context: Context) {
    private val prefs = context.getSharedPreferences("eve_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings = _settings.asStateFlow()

    private fun loadSettings(): EveSettings {
        val clockSizeStr = prefs.getString("clock_size", ClockSize.LARGE.name)
        val clockSize = try { ClockSize.valueOf(clockSizeStr!!) } catch (e: Exception) { ClockSize.LARGE }
        
        val blurIntensityStr = prefs.getString("blur_intensity", BlurIntensity.BALANCED.name)
        val blurIntensity = try { BlurIntensity.valueOf(blurIntensityStr!!) } catch (e: Exception) { BlurIntensity.BALANCED }

        return EveSettings(
            clockSize = clockSize,
            isClockDynamic = prefs.getBoolean("clock_dynamic", true),
            personalClockColor = prefs.getInt("clock_personal_color", 0xFFFFFFFF.toInt()),
            blurIntensity = blurIntensity,
            animationsEnabled = prefs.getBoolean("animations_enabled", true),
            appDrawerColumns = prefs.getInt("app_drawer_cols", 4),
            appDrawerRows = prefs.getInt("app_drawer_rows", 5)
        )
    }

    fun updateSettings(update: (EveSettings) -> EveSettings) {
        _settings.update { current ->
            val next = update(current)
            saveSettings(next)
            next
        }
    }

    private fun saveSettings(s: EveSettings) {
        prefs.edit().apply {
            putString("clock_size", s.clockSize.name)
            putBoolean("clock_dynamic", s.isClockDynamic)
            putInt("clock_personal_color", s.personalClockColor)
            putString("blur_intensity", s.blurIntensity.name)
            putBoolean("animations_enabled", s.animationsEnabled)
            putInt("app_drawer_cols", s.appDrawerColumns)
            putInt("app_drawer_rows", s.appDrawerRows)
            apply()
        }
    }
}
