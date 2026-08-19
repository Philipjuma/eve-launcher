package com.haven.evelauncher.platform.notifications

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class EveNotificationListenerService : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        instance = this
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        instance = null
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
    }

    companion object {
        private var instance: EveNotificationListenerService? = null

        fun getActiveNotifications(): List<StatusBarNotification> {
            return try {
                instance?.activeNotifications?.toList() ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}
