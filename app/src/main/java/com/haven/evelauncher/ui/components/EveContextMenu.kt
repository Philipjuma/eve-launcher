package com.haven.evelauncher.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.haven.evelauncher.ui.theme.EveTypography

data class ContextAction(
    val label: String,
    val icon: ImageVector? = null,
    val onClick: () -> Unit,
    val isDestructive: Boolean = false
)

data class NotificationPreview(
    val title: String,
    val text: String,
    val onClick: () -> Unit
)

@Composable
fun EveContextMenu(
    onDismiss: () -> Unit,
    actions: List<ContextAction>,
    title: String? = null,
    notifications: List<NotificationPreview> = emptyList(),
    modifier: Modifier = Modifier
) {
    Popup(
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.36f)) // Reduced background darkening by 10% (was 0.4f)
                .pointerInput(Unit) {
                    detectTapGestures { onDismiss() }
                },
            contentAlignment = Alignment.Center
        ) {
            val scale by animateFloatAsState(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessHigh
                ),
                label = "menuScale"
            )

            EveGlassSurface(
                modifier = modifier
                    .width(420.dp) // Increased by 40% (was 300.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .animateContentSize(),
                cornerRadius = 40.dp, // Scaled corner radius
                isDark = true
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    if (title != null) {
                        Text(
                            text = title.uppercase(),
                            style = EveTypography.Metadata.copy(
                                color = Color(0xFFA8FFD0),
                                letterSpacing = 1.5.sp,
                                fontSize = 14.sp
                            ),
                            modifier = Modifier.padding(start = 12.dp, top = 4.dp, bottom = 12.dp)
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            thickness = 0.5.dp,
                            color = Color.White.copy(alpha = 0.2f)
                        )
                    }

                    if (notifications.isNotEmpty()) {
                        Column(modifier = Modifier.fillMaxWidth().heightIn(max = 280.dp)) {
                            notifications.forEach { notif ->
                                NotificationPreviewItem(notif, onDismiss)
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    thickness = 0.5.dp,
                                    color = Color.White.copy(alpha = 0.1f)
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                    
                    actions.forEachIndexed { index, action ->
                        ContextActionItem(action = action, onActionClick = onDismiss)
                        if (index < actions.size - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                thickness = 0.5.dp,
                                color = Color.White.copy(alpha = 0.1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationPreviewItem(notif: NotificationPreview, onActionClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable {
                notif.onClick()
                onActionClick()
            }
            .padding(16.dp)
    ) {
        Text(
            text = notif.title,
            style = EveTypography.WidgetPrimary.copy(color = Color.White, fontSize = 16.sp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = notif.text,
            style = EveTypography.WidgetSecondary.copy(
                color = Color.White.copy(alpha = 0.75f),
                lineHeight = 20.sp
            ),
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ContextActionItem(
    action: ContextAction,
    onActionClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable {
                action.onClick()
                onActionClick()
            }
            .padding(vertical = 20.dp, horizontal = 24.dp), // Substantial padding
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (action.icon != null) {
            Icon(
                imageVector = action.icon,
                contentDescription = null,
                tint = if (action.isDestructive) Color.Red.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(20.dp))
        }
        Text(
            text = action.label,
            style = EveTypography.WidgetPrimary.copy(
                color = if (action.isDestructive) Color.Red.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.95f),
                fontSize = 18.sp // More prominent
            )
        )
    }
}
