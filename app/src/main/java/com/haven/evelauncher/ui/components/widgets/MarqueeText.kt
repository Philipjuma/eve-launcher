package com.haven.evelauncher.ui.components.widgets

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow

/**
 * Premium Marquee Text for Eve. 
 * Automatically scrolls if content exceeds container width.
 */
@Composable
fun PremiumMarqueeText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier
) {
    var textWidth by remember { mutableStateOf(0) }
    var containerWidth by remember { mutableStateOf(0) }
    
    val animationProgress = remember { Animatable(0f) }
    
    LaunchedEffect(textWidth, containerWidth, text) {
        if (textWidth > containerWidth && containerWidth > 0) {
            animationProgress.snapTo(0f)
            animationProgress.animateTo(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = (textWidth * 25).coerceAtLeast(4000), 
                        easing = LinearEasing
                    ),
                    repeatMode = RepeatMode.Restart,
                    initialStartOffset = StartOffset(2000)
                )
            )
        } else {
            animationProgress.snapTo(0f)
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clipToBounds()
            .layout { measurable, constraints ->
                containerWidth = constraints.maxWidth
                val placeable = measurable.measure(constraints)
                layout(constraints.maxWidth, placeable.height) {
                    placeable.placeRelative(0, 0)
                }
            }
    ) {
        Text(
            text = text,
            style = style,
            maxLines = 1,
            overflow = TextOverflow.Visible,
            onTextLayout = { textLayoutResult ->
                textWidth = textLayoutResult.size.width
            },
            modifier = Modifier.layout { measurable, constraints ->
                val placeable = measurable.measure(constraints.copy(maxWidth = Int.MAX_VALUE))
                layout(placeable.width, placeable.height) {
                    val x = if (textWidth > containerWidth) {
                        -(animationProgress.value * (textWidth - containerWidth + 80))
                    } else 0f
                    placeable.placeRelative(x.toInt(), 0)
                }
            }
        )
    }
}
