package com.haven.evelauncher.data.media

import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import com.haven.evelauncher.core.widget.*
import com.haven.evelauncher.platform.notifications.EveNotificationListenerService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*

class MediaDataSource(private val context: Context) : EveDataSource {
    override val sourceType: EveDataSourceType = EveDataSourceType.MUSIC

    private val mediaSessionManager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
    private val componentName = ComponentName(context, EveNotificationListenerService::class.java)

    override fun getCandidates(): Flow<List<EveWidgetCandidate>> = callbackFlow {
        val updateCandidates = {
            try {
                val controllers = mediaSessionManager.getActiveSessions(componentName)
                val candidates = controllers.mapNotNull { controller ->
                    val metadata = controller.metadata
                    val playbackState = controller.playbackState
                    
                    if (playbackState?.state == PlaybackState.STATE_PLAYING || 
                        playbackState?.state == PlaybackState.STATE_PAUSED) {
                        
                        val title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE) ?: "No Title"
                        val artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: "Unknown Artist"
                        val isLive = playbackState.state == PlaybackState.STATE_PLAYING

                         EveWidgetCandidate(
                            id = "media_${controller.packageName}",
                            category = EveCategory.WORLD,
                            semanticGroupId = "media_family",
                            deduplicationKey = "media_now",
                            title = title,
                            subtitle = artist,
                            icon = EveIcon(emoji = "🎵"),
                            priority = if (isLive) 95 else 40,
                            relevanceScore = if (isLive) 0.95f else 0.4f,
                            isLive = isLive,
                            isPersonal = true,
                            sourceType = EveDataSourceType.MUSIC,
                            packageName = controller.packageName // Target actions correctly
                        )
                    } else null
                }
                trySend(candidates)
            } catch (e: SecurityException) {
                trySend(emptyList())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val sessionListener = MediaSessionManager.OnActiveSessionsChangedListener { 
            updateCandidates()
        }

        try {
            mediaSessionManager.addOnActiveSessionsChangedListener(sessionListener, componentName)
            updateCandidates()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        awaitClose {
            try {
                mediaSessionManager.removeOnActiveSessionsChangedListener(sessionListener)
            } catch (e: Exception) { }
        }
    }
}
