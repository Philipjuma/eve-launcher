package com.haven.evelauncher.ui.components.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haven.evelauncher.core.widget.EveAction
import com.haven.evelauncher.core.widget.EveActionExecutor
import com.haven.evelauncher.core.widget.EveWidgetCandidate
import com.haven.evelauncher.design.motion.evePressable
import com.haven.evelauncher.ui.components.EveGlassCard
import com.haven.evelauncher.ui.theme.EveTypography

@Composable
fun MediaWidget(
    item: EveWidgetCandidate?,
    modifier: Modifier = Modifier,
    contentColor: Color = Color.White
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    
    EveGlassCard(
        modifier = modifier
            .evePressable()
            .clickable(enabled = item != null) {
                item?.let { EveActionExecutor.execute(context, it) }
            }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (item != null) {
                // Background "Art" Simulation
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF2C3E50).copy(alpha = 0.5f), Color(0xFF000000).copy(alpha = 0.8f))
                            )
                        )
                )

                // Content Overlay
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Header (Title/Artist)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = item.title,
                            style = EveTypography.WidgetHeadline.copy(
                                color = contentColor,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        item.subtitle?.let {
                            Text(
                                text = it,
                                style = EveTypography.WidgetSecondary.copy(
                                    color = contentColor.copy(alpha = 0.7f),
                                    fontSize = 12.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Main Controls on top of "Art" area
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MediaControlIcon(
                            icon = Icons.Default.FastRewind,
                            contentColor = contentColor,
                            onClick = { 
                                EveActionExecutor.execute(context, item.copy(action = EveAction.MediaPrevious)) 
                            }
                        )
                        
                        MediaControlIcon(
                            icon = if (item.isLive) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentColor = contentColor,
                            isLarge = true,
                            onClick = { 
                                EveActionExecutor.execute(context, item.copy(action = EveAction.MediaPlayPause)) 
                            }
                        )
                        
                        MediaControlIcon(
                            icon = Icons.Default.FastForward,
                            contentColor = contentColor,
                            onClick = { 
                                EveActionExecutor.execute(context, item.copy(action = EveAction.MediaNext)) 
                            }
                        )
                    }
                    
                    // Progress bar placeholder or just a hint of "Listening Now"
                    Text(
                        text = if (item.isLive) "Listening Now" else "Paused",
                        style = EveTypography.Metadata.copy(
                            color = (if (contentColor == Color.White) Color(0xFFA8FFD0) else contentColor).copy(alpha = 0.6f),
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        )
                    )
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "HONESTY",
                        style = EveTypography.Metadata.copy(color = contentColor.copy(alpha = 0.2f), letterSpacing = 2.sp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MediaControlIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentColor: Color,
    isLarge: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(if (isLarge) 52.dp else 40.dp)
            .evePressable()
            .clip(CircleShape)
            .background(contentColor.copy(alpha = 0.12f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor.copy(alpha = 0.9f),
            modifier = Modifier.size(if (isLarge) 28.dp else 22.dp)
        )
    }
}
