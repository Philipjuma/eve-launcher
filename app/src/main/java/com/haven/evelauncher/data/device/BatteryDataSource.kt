package com.haven.evelauncher.data.device

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.haven.evelauncher.core.widget.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlin.math.abs

class BatteryDataSource(private val context: Context) : EveDataSource {
    override val sourceType: EveDataSourceType = EveDataSourceType.BATTERY

    private val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    private val currentSamples = mutableListOf<Long>()
    private val maxSamples = 15 // Increased smoothing

    override fun getCandidates(): Flow<List<EveWidgetCandidate>> = flow {
        while (true) {
            val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus = context.registerReceiver(null, filter)

            val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val percentage = (level * 100 / scale.toFloat()).toInt()

            val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

            // Hardware Telemetry
            val chargeCounter = batteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER) // µAh
            val currentNow = batteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) // µA
            val voltage = batteryStatus?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) ?: -1 // mV

            // Instantaneous Power Draw
            val powerWatts = if (voltage > 0) (abs(currentNow) / 1_000_000.0) * (voltage / 1_000.0) else 0.0

            // Smoothing Current
            if (currentNow != Long.MIN_VALUE && currentNow != 0L) {
                currentSamples.add(currentNow)
                if (currentSamples.size > maxSamples) currentSamples.removeAt(0)
            }

            val avgCurrent = if (currentSamples.isNotEmpty()) currentSamples.average() else 0.0
            
            var displayTitle = "$percentage%"
            var displaySubtitle = if (isCharging) "Charging" else "Battery"
            
            // Premium Estimation using smoothed hardware telemetry
            if (abs(avgCurrent) > 500) { // Threshold for valid flow
                if (!isCharging && avgCurrent < 0) {
                    val remainingHours = (chargeCounter.toDouble() / abs(avgCurrent))
                    val hours = remainingHours.toInt()
                    val minutes = ((remainingHours - hours) * 60).toInt()
                    
                    if (hours in 1..72) {
                        displayTitle = "${hours}h ${minutes}m"
                        displaySubtitle = "battery remaining"
                    }
                } else if (isCharging && avgCurrent > 0) {
                    // Estimate time to full using current percentage as baseline for capacity
                    if (percentage in 1..99) {
                        val totalCapacityEst = (chargeCounter.toDouble() / (percentage / 100.0))
                        val remainingCharge = totalCapacityEst - chargeCounter
                        val timeToFullHours = remainingCharge / avgCurrent
                        
                        val hours = timeToFullHours.toInt()
                        val minutes = ((timeToFullHours - hours) * 60).toInt()
                        
                        if (timeToFullHours < 12) {
                            displayTitle = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
                            displaySubtitle = "until full"
                        }
                    }
                }
            }

            emit(listOf(
                EveWidgetCandidate(
                    id = "battery_status",
                    category = EveCategory.PERSONAL,
                    semanticGroupId = "device_family",
                    deduplicationKey = "battery",
                    title = displayTitle,
                    subtitle = displaySubtitle,
                    icon = EveIcon(emoji = if (isCharging) "⚡" else "🔋"),
                    priority = 70,
                    sourceType = EveDataSourceType.BATTERY,
                    relevanceScore = if (percentage < 20) 0.95f else 0.6f
                )
            ))
            delay(30000)
        }
    }
}
