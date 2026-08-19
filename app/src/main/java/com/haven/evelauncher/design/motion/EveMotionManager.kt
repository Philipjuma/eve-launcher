package com.haven.evelauncher.design.motion

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class EveMotionState(
    val pitch: Float = 0f, 
    val roll: Float = 0f,
    val lux: Float = 200f
)

class EveMotionManager(context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

    private val _motionState = MutableStateFlow(EveMotionState())
    val motionState: StateFlow<EveMotionState> = _motionState.asStateFlow()

    fun start() {
        rotationSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
        lightSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        when (event?.sensor?.type) {
            Sensor.TYPE_ROTATION_VECTOR -> {
                val rotationMatrix = FloatArray(9)
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                val orientation = FloatArray(3)
                SensorManager.getOrientation(rotationMatrix, orientation)
                
                val pitch = (orientation[1] / (Math.PI / 2)).toFloat().coerceIn(-1f, 1f)
                val roll = (orientation[2] / (Math.PI / 2)).toFloat().coerceIn(-1f, 1f)
                
                _motionState.value = _motionState.value.copy(pitch = pitch, roll = roll)
            }
            Sensor.TYPE_LIGHT -> {
                _motionState.value = _motionState.value.copy(lux = event.values[0])
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
