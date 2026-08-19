package com.haven.evelauncher.data.alarm

import android.app.AlarmManager
import android.content.Context
import com.haven.evelauncher.core.widget.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.*

class AlarmDataSource(private val context: Context) : EveDataSource {
    override val sourceType: EveDataSourceType = EveDataSourceType.ALARM

    override fun getCandidates(): Flow<List<EveWidgetCandidate>> = flow {
        while (true) {
            val candidate = getNextAlarm()
            emit(if (candidate != null) listOf(candidate) else emptyList())
            delay(300000) // 5 minutes refresh
        }
    }

    private fun getNextAlarm(): EveWidgetCandidate? {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val nextAlarm = alarmManager.nextAlarmClock ?: return null
        
        val now = System.currentTimeMillis()
        val timeUntil = nextAlarm.triggerTime - now
        
        // Only show alarm widget if it's within the next 4 hours
        // User requested to only show if "in notification", which implies urgency.
        if (timeUntil > 14400000) return null 

        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val formatted = sdf.format(Date(nextAlarm.triggerTime))

        return EveWidgetCandidate(
            id = "alarm_${nextAlarm.triggerTime}",
            category = EveCategory.ALARM,
            semanticGroupId = "alarm_family",
            deduplicationKey = "alarm_${nextAlarm.triggerTime}",
            title = formatted,
            subtitle = "Upcoming Alarm",
            icon = EveIcon.Alarm,
            priority = 70, // High priority
            relevanceScore = 0.8f,
            urgencyScore = 0.4f,
            isPersonal = true,
            sourceType = EveDataSourceType.ALARM,
            createdAt = System.currentTimeMillis(),
            expiresAt = nextAlarm.triggerTime
        )
    }
}
