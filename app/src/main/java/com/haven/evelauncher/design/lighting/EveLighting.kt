package com.haven.evelauncher.design.lighting

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.haven.evelauncher.design.motion.EveMotionManager
import com.haven.evelauncher.design.motion.EveMotionState

@Immutable
data class EveLightingState(
    val lightOffset: Offset = Offset(0.5f, 0.5f), 
    val highlightIntensity: Float = 0.5f,
    val ambientBrightness: Float = 1.0f,
    val accentColor: Color = Color.White
)

val LocalEveLighting = staticCompositionLocalOf { EveLightingState() }

@Composable
fun EveLightingProvider(
    motionManager: EveMotionManager,
    content: @Composable () -> Unit
) {
    val motionState by motionManager.motionState.collectAsState()
    
    val lightingState = remember(motionState) {
        val x = (1f - motionState.roll) / 2f
        val y = (1f - motionState.pitch) / 2f
        
        // Very subtle brightness response: 0.8f to 1.2f
        val brightness = (0.8f + (motionState.lux / 2000f)).coerceIn(0.8f, 1.2f)
        
        EveLightingState(
            lightOffset = Offset(x.coerceIn(0f, 1f), y.coerceIn(0f, 1f)),
            highlightIntensity = (0.3f + (Math.abs(motionState.pitch) + Math.abs(motionState.roll)) * 0.2f) * brightness,
            ambientBrightness = brightness
        )
    }

    CompositionLocalProvider(LocalEveLighting provides lightingState) {
        content()
    }
}
