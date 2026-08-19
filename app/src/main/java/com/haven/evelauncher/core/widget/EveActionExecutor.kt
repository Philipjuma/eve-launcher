package com.haven.evelauncher.core.widget

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.session.MediaSessionManager
import android.net.Uri
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.provider.Settings
import android.view.KeyEvent
import android.widget.Toast
import com.haven.evelauncher.platform.notifications.EveNotificationListenerService

object EveActionExecutor {
    fun execute(context: Context, candidate: EveWidgetCandidate) {
        try {
            val action = candidate.action
            if (action != null) {
                when (action.type) {
                    EveAction.ActionType.LAUNCH_APP -> {
                        action.payload?.let { pkg ->
                            val intent = context.packageManager.getLaunchIntentForPackage(pkg)
                            intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            if (intent != null) context.startActivity(intent)
                        }
                    }
                    EveAction.ActionType.OPEN_CALENDAR -> {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = CalendarContract.CONTENT_URI.buildUpon().appendPath("time").build()
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    }
                    EveAction.ActionType.OPEN_ALARM -> {
                        val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    }
                    EveAction.ActionType.OPEN_SETTINGS -> {
                        val intent = Intent(Settings.ACTION_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    }
                    EveAction.ActionType.OPEN_URL -> {
                        action.payload?.let { url ->
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        }
                    }
                    EveAction.ActionType.MEDIA_PLAY_PAUSE -> handleMediaAction(context, candidate, "TOGGLE")
                    EveAction.ActionType.MEDIA_NEXT -> handleMediaAction(context, candidate, "NEXT")
                    EveAction.ActionType.MEDIA_PREVIOUS -> handleMediaAction(context, candidate, "PREV")
                    else -> { /* Handle others */ }
                }
            } else {
                // Default fallbacks based on source type
                when (candidate.sourceType) {
                    EveDataSourceType.BATTERY -> {
                        val intent = Intent(Intent.ACTION_POWER_USAGE_SUMMARY).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    }
                    EveDataSourceType.MUSIC -> {
                        // Open specific app if packageName is available
                        val pkg = candidate.packageName ?: ""
                        val intent = if (pkg.isNotEmpty()) {
                            context.packageManager.getLaunchIntentForPackage(pkg)
                        } else {
                            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_MUSIC)
                        }
                        
                        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        if (intent != null) {
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Last resort
                            }
                        }
                    }
                    EveDataSourceType.CALENDAR -> {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = CalendarContract.CONTENT_URI.buildUpon().appendPath("time").build()
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    }
                    else -> {}
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Could not execute action", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleMediaAction(context: Context, candidate: EveWidgetCandidate, type: String) {
        val mediaSessionManager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
        val componentName = ComponentName(context, EveNotificationListenerService::class.java)
        
        try {
            val controllers = mediaSessionManager.getActiveSessions(componentName)
            // Try to find the matching controller by package if available, else first one
            val controller = if (candidate.packageName != null) {
                controllers.find { it.packageName == candidate.packageName } ?: controllers.firstOrNull()
            } else {
                controllers.firstOrNull()
            }

            if (controller != null) {
                val controls = controller.transportControls
                when(type) {
                    "TOGGLE" -> {
                        val state = controller.playbackState?.state
                        if (state == android.media.session.PlaybackState.STATE_PLAYING) controls.pause()
                        else controls.play()
                    }
                    "NEXT" -> controls.skipToNext()
                    "PREV" -> controls.skipToPrevious()
                }
            } else {
                // Fallback to media buttons if session lost but candidate exists
                val keyCode = when(type) {
                    "TOGGLE" -> KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                    "NEXT" -> KeyEvent.KEYCODE_MEDIA_NEXT
                    "PREV" -> KeyEvent.KEYCODE_MEDIA_PREVIOUS
                    else -> 0
                }
                if (keyCode != 0) sendMediaKey(context, keyCode)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun sendMediaKey(context: Context, keyCode: Int) {
        val mediaSessionManager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
        val componentName = ComponentName(context, EveNotificationListenerService::class.java)
        
        try {
            val controllers = mediaSessionManager.getActiveSessions(componentName)
            if (controllers.isNotEmpty()) {
                val controller = controllers[0]
                val downEvent = KeyEvent(KeyEvent.ACTION_DOWN, keyCode)
                controller.dispatchMediaButtonEvent(downEvent)
                val upEvent = KeyEvent(KeyEvent.ACTION_UP, keyCode)
                controller.dispatchMediaButtonEvent(upEvent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
