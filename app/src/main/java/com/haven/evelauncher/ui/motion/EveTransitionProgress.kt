package com.haven.evelauncher.ui.motion

import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp

@Stable
class EveTransitionProgress(
    initial: Float = 0f
) {
    private val progressAnimatable = Animatable(initial)

    val progress: Float
        get() = progressAnimatable.value

    val isOpening: Boolean
        get() = progressAnimatable.targetValue == 1f && progressAnimatable.isRunning

    val isClosing: Boolean
        get() = progressAnimatable.targetValue == 0f && progressAnimatable.isRunning

    suspend fun snapTo(value: Float) {
        progressAnimatable.snapTo(value.coerceIn(0f, 1f))
    }

    suspend fun animateTo(
        target: Float,
        initialVelocity: Float = 0f,
        spec: AnimationSpec<Float> = spring(
            dampingRatio = Spring.DampingRatioNoBouncy, // More stable
            stiffness = Spring.StiffnessMedium // Quicker
        )
    ) {
        progressAnimatable.animateTo(
            targetValue = target.coerceIn(0f, 1f),
            initialVelocity = initialVelocity,
            animationSpec = spec
        )
    }
}

@Composable
fun rememberEveTransitionProgress(): EveTransitionProgress =
    remember { EveTransitionProgress() }

interface EveDrawerTransitionSpec {
    fun homeScale(progress: Float): Float
    fun homeAlpha(progress: Float): Float
    fun drawerScale(progress: Float): Float
    fun drawerAlpha(progress: Float): Float
    fun dockOffset(progress: Float): Float
    fun blurRadius(progress: Float): Dp
}

object EveDrawerTransitionDefault : EveDrawerTransitionSpec {
    override fun homeScale(progress: Float) = lerp(1f, 1.08f, progress)
    override fun homeAlpha(progress: Float) = lerp(1f, 0.6f, progress)
    override fun drawerScale(progress: Float) = lerp(0.92f, 1f, progress)
    override fun drawerAlpha(progress: Float) =
        lerp(0f, 1f, (progress / 0.5f).coerceIn(0f, 1f)) 
    override fun dockOffset(progress: Float) = lerp(0f, 40f, progress)
    override fun blurRadius(progress: Float): Dp {
        val step = (progress * 5).toInt().coerceIn(0, 5)
        return (step * 4).dp
    }
}

object EveDrawerTransitionReducedMotion : EveDrawerTransitionSpec {
    override fun homeScale(progress: Float) = 1f
    override fun homeAlpha(progress: Float) = lerp(1f, 0.9f, progress)
    override fun drawerScale(progress: Float) = 1f
    override fun drawerAlpha(progress: Float) = progress
    override fun dockOffset(progress: Float) = 0f
    override fun blurRadius(progress: Float) = 0.dp
}
