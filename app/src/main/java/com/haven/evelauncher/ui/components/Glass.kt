package com.haven.evelauncher.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.haven.evelauncher.data.repository.BlurIntensity
import com.haven.evelauncher.design.lighting.LocalEveLighting
import com.haven.evelauncher.design.settings.LocalEveSettings
import com.haven.evelauncher.ui.theme.GlassWhite
import com.haven.evelauncher.ui.theme.GlassWhiteStroke

@Composable
fun EveGlassSurface(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    alpha: Float = 1f,
    isDark: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val lighting = LocalEveLighting.current
    val lightOffset = lighting.lightOffset
    val settings = LocalEveSettings.current

    val baseAlpha = if (isDark) 0.6f else 0.28f
    val baseColor = if (isDark) Color.Black.copy(alpha = baseAlpha * alpha) else GlassWhite.copy(alpha = baseAlpha * alpha)
    val strokeColor = if (isDark) Color.White.copy(alpha = 0.15f * alpha) else GlassWhiteStroke.copy(alpha = 0.7f * alpha)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .drawBehind {
                // Base Glass Tint with dynamic reaction to light
                val brush = Brush.linearGradient(
                    0.0f to baseColor.copy(alpha = baseColor.alpha + lighting.highlightIntensity * 0.15f),
                    1.0f to baseColor.copy(alpha = baseColor.alpha * 0.6f),
                    start = Offset(lightOffset.x * size.width, lightOffset.y * size.height),
                    end = Offset((1f - lightOffset.x) * size.width, (1f - lightOffset.y) * size.height)
                )
                drawRect(brush = brush)
                
                // Thicker, more reactive edges for "Simulated Refraction"
                val strokeBrush = Brush.linearGradient(
                    colors = listOf(
                        strokeColor.copy(alpha = strokeColor.alpha + lighting.highlightIntensity * 0.3f),
                        strokeColor.copy(alpha = strokeColor.alpha * 0.2f)
                    ),
                    start = Offset(lightOffset.x * size.width, lightOffset.y * size.height),
                    end = Offset((1f - lightOffset.x) * size.width, (1f - lightOffset.y) * size.height)
                )
                drawRoundRect(
                    brush = strokeBrush,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius.toPx()),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
            .drawWithContent {
                drawContent()
                
                if (!isDark) {
                    // Specular Highlight / Glare
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.18f * lighting.highlightIntensity),
                                Color.Transparent
                            ),
                            center = Offset(
                                lightOffset.x * size.width,
                                lightOffset.y * size.height
                            ),
                            radius = size.maxDimension * 0.7f
                        ),
                        radius = size.maxDimension * 0.7f,
                        center = Offset(
                            lightOffset.x * size.width,
                            lightOffset.y * size.height
                        ),
                        blendMode = BlendMode.Screen
                    )
                }
            }
    ) {
        content()
    }
}

@Composable
fun EveGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    content: @Composable BoxScope.() -> Unit
) {
    EveGlassSurface(
        modifier = modifier,
        cornerRadius = cornerRadius,
        content = content
    )
}
