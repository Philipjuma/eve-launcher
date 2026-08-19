package com.haven.evelauncher.data.weather

import com.haven.evelauncher.core.widget.*
import com.haven.evelauncher.data.location.EveLocationManager
import kotlinx.coroutines.flow.*

data class WeatherSnapshot(
    val temp: Float,
    val condition: String,
    val weatherCode: Int,
    val precipitationProb: Int,
    val sunrise: String? = null, // HH:mm
    val sunset: String? = null,  // HH:mm
    val timestamp: Long = System.currentTimeMillis()
)

interface WeatherRepository {
    suspend fun getCurrentWeather(latitude: Double, longitude: Double): WeatherSnapshot?
}

class WeatherDataSource(
    private val repository: WeatherRepository,
    private val locationManager: EveLocationManager
) : EveDataSource {
    override val sourceType: EveDataSourceType = EveDataSourceType.WEATHER

    override fun getCandidates(): Flow<List<EveWidgetCandidate>> = 
        locationManager.currentLocation
            .filterNotNull()
            .map { location ->
                val weather = repository.getCurrentWeather(location.latitude, location.longitude)
                val candidates = mutableListOf<EveWidgetCandidate>()
                
                if (weather != null) {
                    // 1. Current Weather
                    candidates.add(EveWidgetCandidate(
                        id = "weather_current",
                        category = EveCategory.ENVIRONMENT,
                        semanticGroupId = "weather_family",
                        deduplicationKey = "weather_now",
                        title = "${weather.temp.toInt()}°C",
                        subtitle = weather.condition,
                        icon = EveIcon(emoji = mapCodeToEmoji(weather.weatherCode)),
                        priority = 80,
                        relevanceScore = 0.9f,
                        sourceType = EveDataSourceType.WEATHER,
                        createdAt = weather.timestamp
                    ))
                    
                    // 2. Astronomy (Sunrise/Sunset)
                    if (weather.sunset != null) {
                        candidates.add(EveWidgetCandidate(
                            id = "astronomy_sunset",
                            category = EveCategory.ENVIRONMENT,
                            semanticGroupId = "weather_family",
                            deduplicationKey = "sunset",
                            title = "Sunset",
                            subtitle = "${weather.sunset}",
                            icon = EveIcon(emoji = "🌇"),
                            priority = 40,
                            relevanceScore = 0.5f,
                            sourceType = EveDataSourceType.ASTRONOMY
                        ))
                    }
                }
                candidates
            }

    private fun mapCodeToEmoji(code: Int): String = when (code) {
        0 -> "☀️"
        1, 2, 3 -> "🌤️"
        45, 48 -> "🌫️"
        51, 53, 55 -> "🌦️"
        61, 63, 65 -> "🌧️"
        71, 73, 75 -> "❄️"
        80, 81, 82 -> "🌦️"
        95, 96, 99 -> "⛈️"
        else -> "🌡️"
    }
}
