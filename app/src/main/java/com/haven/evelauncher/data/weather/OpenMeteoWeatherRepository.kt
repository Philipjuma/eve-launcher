package com.haven.evelauncher.data.weather

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.text.SimpleDateFormat
import java.util.*

interface OpenMeteoApi {
    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "temperature_2m,weather_code,precipitation_probability",
        @Query("daily") daily: String = "sunrise,sunset",
        @Query("timezone") timezone: String = "auto"
    ): OpenMeteoResponse
}

data class OpenMeteoResponse(
    val current: CurrentData,
    val daily: DailyData? = null
)

data class CurrentData(
    val temperature_2m: Float,
    val weather_code: Int,
    val precipitation_probability: Int? = null
)

data class DailyData(
    val sunrise: List<String>,
    val sunset: List<String>
)

class OpenMeteoWeatherRepository : WeatherRepository {
    private val api = Retrofit.Builder()
        .baseUrl("https://api.open-meteo.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(OpenMeteoApi::class.java)

    override suspend fun getCurrentWeather(latitude: Double, longitude: Double): WeatherSnapshot? {
        return try {
            val response = api.getForecast(latitude, longitude)
            
            // Parse HH:mm from ISO 8601 (e.g., "2024-08-16T06:21")
            val sunrise = response.daily?.sunrise?.firstOrNull()?.split("T")?.lastOrNull()
            val sunset = response.daily?.sunset?.firstOrNull()?.split("T")?.lastOrNull()

            WeatherSnapshot(
                temp = response.current.temperature_2m,
                condition = mapCodeToCondition(response.current.weather_code),
                weatherCode = response.current.weather_code,
                precipitationProb = response.current.precipitation_probability ?: 0,
                sunrise = sunrise,
                sunset = sunset
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun mapCodeToCondition(code: Int): String = when (code) {
        0 -> "Clear sky"
        1, 2, 3 -> "Mainly clear"
        45, 48 -> "Fog"
        51, 53, 55 -> "Drizzle"
        61, 63, 65 -> "Rain"
        71, 73, 75 -> "Snow fall"
        80, 81, 82 -> "Rain showers"
        95, 96, 99 -> "Thunderstorm"
        else -> "Unknown"
    }
}
