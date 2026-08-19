package com.haven.evelauncher.data.repository

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.*

class HealthRepository(private val context: Context) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val prefs = context.getSharedPreferences("health_prefs", Context.MODE_PRIVATE)

    fun getTodaySteps(): Flow<Int> = callbackFlow {
        if (stepSensor == null) {
            trySend(0)
            close()
            return@callbackFlow
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
                    try {
                        val totalSteps = event.values[0].toInt()
                        val todaySteps = calculateTodaySteps(totalSteps)
                        trySend(todaySteps)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        try {
            sensorManager.registerListener(listener, stepSensor, SensorManager.SENSOR_DELAY_UI)
        } catch (e: Exception) {
            e.printStackTrace()
            close(e)
        }

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }

    private fun calculateTodaySteps(totalSteps: Int): Int {
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_YEAR)
        val savedDay = prefs.getInt("last_day", -1)
        var midnightSteps = prefs.getInt("midnight_steps", -1)

        if (today != savedDay) {
            // New day: reset midnight steps
            midnightSteps = totalSteps
            prefs.edit().putInt("last_day", today).putInt("midnight_steps", midnightSteps).apply()
        }

        if (midnightSteps == -1) {
            midnightSteps = totalSteps
            prefs.edit().putInt("midnight_steps", midnightSteps).apply()
        }

        return (totalSteps - midnightSteps).coerceAtLeast(0)
    }
}
