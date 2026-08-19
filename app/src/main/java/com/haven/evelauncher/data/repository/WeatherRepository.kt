package com.haven.evelauncher.data.repository

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

data class WeatherData(
    val temp: Float,
    val condition: String,
    val icon: String,
    val timestamp: Long = System.currentTimeMillis()
)

interface OpenMeteoApi {
    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("current_weather") current: Boolean = true
    ): OpenMeteoResponse
}

data class OpenMeteoResponse(
    val current_weather: CurrentWeather
)

data class CurrentWeather(
    val temperature: Float,
    val weathercode: Int
)

class WeatherRepository {
    private val api = Retrofit.Builder()
        .baseUrl("https://api.open-meteo.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(OpenMeteoApi::class.java)

    suspend fun getWeather(lat: Double, lon: Double): WeatherData? {
        return try {
            val response = api.getForecast(lat, lon)
            WeatherData(
                temp = response.current_weather.temperature,
                condition = mapCodeToCondition(response.current_weather.weathercode),
                icon = mapCodeToIcon(response.current_weather.weathercode)
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

    private fun mapCodeToIcon(code: Int): String = when (code) {
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
