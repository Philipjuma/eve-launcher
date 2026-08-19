package com.haven.evelauncher.data.notifications

import android.app.Notification
import android.content.Context
import com.haven.evelauncher.core.widget.*
import com.haven.evelauncher.platform.notifications.EveNotificationListenerService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*

class NotificationDataSource(private val context: Context) : EveDataSource {
    override val sourceType: EveDataSourceType = EveDataSourceType.NOTIFICATION

    override fun getCandidates(): Flow<List<EveWidgetCandidate>> = flow {
        while (true) {
            val notifications = EveNotificationListenerService.getActiveNotifications()

            val candidates = notifications
                .filter { sbn ->
                    val n = sbn.notification
                    val isSilent = n.priority <= Notification.PRIORITY_LOW
                    val isSystem = sbn.packageName == "android" || sbn.packageName == "com.android.systemui"
                    !sbn.isOngoing && !isSilent && !isSystem
                }
                .mapNotNull { sbn ->
                    val extras = sbn.notification.extras
                    val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
                    val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
                    
                    // Filter out noise
                    if (title.contains("Scheduled Digest", ignoreCase = true) || 
                        text.contains("Scheduled Digest", ignoreCase = true)) return@mapNotNull null
                        
                    if (title.isEmpty() && text.isEmpty()) return@mapNotNull null

                    EveWidgetCandidate(
                        id = "notif_${sbn.id}_${sbn.packageName}",
                        category = EveCategory.ALERTS,
                        semanticGroupId = "message_family",
                        deduplicationKey = "notif_${sbn.packageName}",
                        title = title,
                        subtitle = text,
                        // We use the package name as vectorName to signal the UI to load the app icon
                        icon = EveIcon(vectorName = sbn.packageName),
                        priority = 90,
                        sourceType = EveDataSourceType.NOTIFICATION,
                        isPersonal = true,
                        createdAt = sbn.postTime
                    )
                }
            emit(candidates)
            delay(5000) // Faster polling for real-time feel
        }
    }
}
