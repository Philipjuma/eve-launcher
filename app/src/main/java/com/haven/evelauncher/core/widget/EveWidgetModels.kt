package com.haven.evelauncher.core.widget

import com.haven.evelauncher.platform.apps.AppInfo

enum class EveCategory {
    ENVIRONMENT, // Weather, Light, Context
    PERSONAL,    // Activity, Battery, Health
    WORLD,       // Music, News, Events
    ORBIT,       // Apps
    ALERTS,      // High-priority notifications
    CALENDAR,
    ALARM
}

enum class EveDataSourceType {
    WEATHER, FOOTBALL, MOVIE, MUSIC, NOTIFICATION, HEALTH, 
    LOCATION, APP_USAGE, ALARM, CALENDAR, INSTALLED_APPS, 
    GREETING, BATTERY, ASTRONOMY, UNKNOWN
}

data class EveIcon(
    val emoji: String? = null,
    val resId: Int? = null,
    val vectorName: String? = null
) {
    companion object {
        val Calendar = EveIcon(emoji = "📅")
        val Alarm = EveIcon(emoji = "⏰")
    }
}

data class EveAction(
    val type: ActionType,
    val payload: String? = null
) {
    enum class ActionType {
        LAUNCH_APP, PENDING_INTENT, OPEN_URL, OPEN_SETTINGS, OPEN_CALENDAR, OPEN_ALARM,
        MEDIA_PLAY_PAUSE, MEDIA_NEXT, MEDIA_PREVIOUS
    }
    
    companion object {
        val OpenCalendar = EveAction(ActionType.OPEN_CALENDAR)
        val OpenAlarm = EveAction(ActionType.OPEN_ALARM)
        val MediaPlayPause = EveAction(ActionType.MEDIA_PLAY_PAUSE)
        val MediaNext = EveAction(ActionType.MEDIA_NEXT)
        val MediaPrevious = EveAction(ActionType.MEDIA_PREVIOUS)
    }
}

data class EveWidgetCandidate(
    val id: String,
    val category: EveCategory,
    val semanticGroupId: String,
    val deduplicationKey: String,

    val title: String,
    val subtitle: String? = null,

    val icon: EveIcon? = null,
    val imageUrl: String? = null,

    val priority: Int = 10,
    val relevanceScore: Float = 0.5f,
    val freshnessScore: Float = 1.0f,
    val urgencyScore: Float = 0.0f,
    val noveltyScore: Float = 0.5f,
    val personalRelevanceScore: Float = 0.5f,

    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long? = null,

    val isLive: Boolean = false,
    val isPersonal: Boolean = false,
    val isRecommendation: Boolean = false,

    val action: EveAction? = null,
    val sourceType: EveDataSourceType,
    val packageName: String? = null // Added for direct app launching
)

sealed class WidgetSlotState {
    object Loading : WidgetSlotState()
    data class Content(val items: List<EveWidgetCandidate>) : WidgetSlotState()
    object Empty : WidgetSlotState()
    data class Error(val reason: String) : WidgetSlotState()
}

data class EveWidgetSlotState(
    val state: WidgetSlotState = WidgetSlotState.Loading,
    val lastUpdated: Long? = null
)

data class EveHomeWidgetsState(
    val slot1: EveWidgetSlotState = EveWidgetSlotState(),
    val slot2: EveWidgetSlotState = EveWidgetSlotState(),
    val slot3: EveWidgetSlotState = EveWidgetSlotState(),
    val slot4: EveWidgetSlotState = EveWidgetSlotState()
)

data class OrbitData(
    val predictiveApps: List<AppInfo> = emptyList()
)

data class EveTimeContext(
    val timeOfDay: String = "UNKNOWN"
)

data class EveContext(
    val greeting: String = "",
    val orbit: OrbitData = OrbitData()
)
