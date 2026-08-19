package com.haven.evelauncher.design.motion

import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

import com.haven.evelauncher.design.settings.LocalEveSettings

/**
 * Reusable Eve Bouncing Animation for App Icons.
 * Motion: jump -> hang -> fall -> squash -> spring back -> settle.
 */
@Composable
fun Modifier.eveBouncingIcon(
    enabled: Boolean = true,
    jumpHeight: Dp = 12.dp,
    delay: Int = 0,
    oneShot: Boolean = false
): Modifier {
    val settings = LocalEveSettings.current
    if (!enabled || !settings.animationsEnabled) return this

    val infiniteTransition = rememberInfiniteTransition(label = "bouncingIcon")
    
    // Translation Y Animation - Optimized for Speed and Impact
    val translationY by if (!oneShot) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 850 // Fast cycle
                    0f at delay using FastOutSlowInEasing
                    2f at delay + 50 using LinearOutSlowInEasing // Quick Anticipation
                    -jumpHeight.value * 0.95f at delay + 250 using LowOutSlowInEasing // Punchy Ascent
                    -jumpHeight.value at delay + 350 using LinearEasing // Apex
                    -jumpHeight.value at delay + 450 // Short Hang
                    0f at delay + 650 using BounceEasing // Fast Impact
                    0f at 850
                },
                repeatMode = RepeatMode.Restart
            ),
            label = "y"
        )
    } else {
        remember { mutableStateOf(0f) }
    }

    // Squash and Stretch Scale Animation
    val scaleX by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 850
                1f at delay
                1.05f at delay + 50
                0.94f at delay + 250 // Stretch
                1f at delay + 350
                1.12f at delay + 650 // Punchy Squash
                1f at delay + 800 // Fast Recovery
                1f at 850
            }
        ),
        label = "scaleX"
    )

    val scaleY by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 850
                1f at delay
                0.95f at delay + 50
                1.06f at delay + 250 // Stretch
                1f at delay + 350
                0.86f at delay + 650 // Punchy Squash
                1f at delay + 800 // Fast Recovery
                1f at 850
            }
        ),
        label = "scaleY"
    )

    return this.graphicsLayer {
        this.translationY = translationY * density
        this.scaleX = scaleX
        this.scaleY = scaleY
    }
}

private val LowOutSlowInEasing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)
private val BounceEasing = CubicBezierEasing(0.33f, 0f, 0.67f, 1f)
