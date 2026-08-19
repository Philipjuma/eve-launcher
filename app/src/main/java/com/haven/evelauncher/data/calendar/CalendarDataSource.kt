package com.haven.evelauncher.data.calendar

import android.content.ContentUris
import android.content.Context
import android.provider.CalendarContract
import com.haven.evelauncher.core.permissions.EvePermission
import com.haven.evelauncher.core.permissions.EvePermissionManager
import com.haven.evelauncher.core.widget.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import java.util.concurrent.TimeUnit

class CalendarDataSource(
    private val context: Context,
    private val permissionManager: EvePermissionManager
) : EveDataSource {
    override val sourceType: EveDataSourceType = EveDataSourceType.CALENDAR

    override fun getCandidates(): Flow<List<EveWidgetCandidate>> = flow {
        while (true) {
            val candidate = getUpcomingEvent()
            emit(if (candidate != null) listOf(candidate) else emptyList())
            delay(300000) // 5 minutes refresh
        }
    }

    private fun getUpcomingEvent(): EveWidgetCandidate? {
        if (!permissionManager.isGranted(EvePermission.CALENDAR)) return null

        val now = System.currentTimeMillis()
        val endOfDay = now + TimeUnit.HOURS.toMillis(18)

        val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
        ContentUris.appendId(builder, now)
        ContentUris.appendId(builder, endOfDay)

        val projection = arrayOf(
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.ALL_DAY
        )

        return try {
            context.contentResolver.query(
                builder.build(), projection, null, null, "begin ASC"
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    val allDay = cursor.getInt(cursor.getColumnIndexOrThrow(CalendarContract.Instances.ALL_DAY)) == 1
                    if (allDay) continue

                    val title = cursor.getString(cursor.getColumnIndexOrThrow(CalendarContract.Instances.TITLE))
                    val begin = cursor.getLong(cursor.getColumnIndexOrThrow(CalendarContract.Instances.BEGIN))
                    val minutesUntil = ((begin - now) / 60000).toInt()

                    if (minutesUntil < 0) continue

                    return EveWidgetCandidate(
                        id = "calendar_$begin",
                        category = EveCategory.CALENDAR,
                        semanticGroupId = "calendar_family",
                        deduplicationKey = "calendar_$begin",
                        title = title ?: "Event",
                        subtitle = "in $minutesUntil min",
                        icon = EveIcon.Calendar,
                        priority = if (minutesUntil <= 60) 90 else 30,
                        relevanceScore = if (minutesUntil <= 60) 0.9f else 0.4f,
                        urgencyScore = if (minutesUntil <= 60) 0.8f else 0.2f,
                        isPersonal = true,
                        action = EveAction.OpenCalendar,
                        sourceType = EveDataSourceType.CALENDAR,
                        createdAt = now,
                        expiresAt = begin
                    )
                }
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
