package com.haven.evelauncher.design.motion

import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput

import com.haven.evelauncher.design.settings.LocalEveSettings

/**
 * Universal Bouncy Interaction Modifier for Eve.
 * Fast compression on press (50-80ms), slower spring return on release (180-300ms).
 */
fun Modifier.evePressable(
    enabled: Boolean = true
): Modifier = composed {
    val settings = LocalEveSettings.current
    if (!enabled || !settings.animationsEnabled) return@composed this

    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = if (pressed) {
            tween(durationMillis = 70, easing = LinearOutSlowInEasing)
        } else {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        },
        label = "pressScale"
    )

    this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }.pointerInput(Unit) {
        awaitPointerEventScope {
            while (true) {
                awaitFirstDown(false)
                pressed = true
                waitForUpOrCancellation()
                pressed = false
            }
        }
    }
}
