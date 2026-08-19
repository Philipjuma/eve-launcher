package com.haven.evelauncher.ui.components

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.haven.evelauncher.design.motion.EveMotion
import com.haven.evelauncher.design.motion.evePressable
import com.haven.evelauncher.platform.apps.AppInfo
import com.haven.evelauncher.ui.HomeViewModel
import com.haven.evelauncher.ui.theme.EveTypography
import kotlinx.coroutines.delay

enum class DockState {
    CLOSED, PEEK, EXPANDED
}

@Composable
fun EveSmartDock(
    viewModel: HomeViewModel,
    dockApps: List<AppInfo>,
    notificationApps: List<AppInfo>,
    onOpenApps: () -> Unit,
    onLaunchApp: (AppInfo) -> Unit,
    onRemoveFromDock: (AppInfo) -> Unit,
    onToggleFavorite: (AppInfo) -> Unit,
    isAppDrawerVisible: Boolean = false,
    modifier: Modifier = Modifier
) {
    var dockState by remember { mutableStateOf(DockState.CLOSED) }
    var selectedAppForMenu by remember { mutableStateOf<AppInfo?>(null) }
    val context = LocalContext.current
    val haptics = androidx.compose.ui.platform.LocalHapticFeedback.current

    LaunchedEffect(selectedAppForMenu) {
        viewModel.setAnyMenuOpen(selectedAppForMenu != null)
    }
    
    // Dynamic Menu Effects
    val menuBlur by animateDpAsState(
        targetValue = if (selectedAppForMenu != null) 32.dp else 0.dp, // Increased from 16.dp
        animationSpec = tween(200),
        label = "menuBlur"
    )
    val menuDim by animateFloatAsState(
        targetValue = if (selectedAppForMenu != null) 0.7f else 0f, // More dim
        animationSpec = tween(200),
        label = "menuDim"
    )

    // Inactivity Timer
    var lastActivity by remember { mutableStateOf(0L) }
    fun poke() { lastActivity = System.currentTimeMillis() }

    LaunchedEffect(isAppDrawerVisible) {
        if (isAppDrawerVisible) {
            dockState = DockState.EXPANDED
        } else {
            dockState = DockState.CLOSED
        }
    }

    LaunchedEffect(dockState, lastActivity) {
        if (dockState != DockState.CLOSED && !isAppDrawerVisible) {
            delay(5000)
            dockState = DockState.CLOSED
        }
    }

    // Eve's Dormant Eyes Logic
    var eyesVisible by remember { mutableStateOf(true) }
    var blinkTrigger by remember { mutableStateOf(false) }
    
    LaunchedEffect(dockState) {
        if (dockState == DockState.CLOSED) {
            delay(500)
            eyesVisible = true
        } else {
            eyesVisible = false
        }
    }

    LaunchedEffect(eyesVisible, dockState) {
        if (eyesVisible && dockState == DockState.CLOSED) {
            while(true) {
                delay((1500 + (Math.random() * 3000)).toLong())
                blinkTrigger = true
                delay(120)
                blinkTrigger = false
                if (Math.random() > 0.7) {
                    delay(80)
                    blinkTrigger = true
                    delay(120)
                    blinkTrigger = false
                }
            }
        }
    }
    
    val eyeAlpha by animateFloatAsState(
        targetValue = if (!eyesVisible) 0f else if (blinkTrigger) 0.2f else 0.9f,
        animationSpec = if (blinkTrigger) tween(80) else tween(600),
        label = "eyeAlpha"
    )
    
    val eyeScaleY by animateFloatAsState(
        targetValue = if (blinkTrigger) 0.1f else 1f,
        animationSpec = if (blinkTrigger) tween(100) else spring(stiffness = Spring.StiffnessLow),
        label = "eyeScale"
    )

    val dockWidth by animateDpAsState(
        targetValue = when (dockState) {
            DockState.CLOSED -> 80.dp
            DockState.PEEK -> 160.dp
            DockState.EXPANDED -> 320.dp
        },
        animationSpec = EveMotion.springHigh(),
        label = "width"
    )

    val dockHeight by animateDpAsState(
        targetValue = if (dockState == DockState.PEEK) 96.dp else 72.dp,
        animationSpec = EveMotion.springHigh(),
        label = "height"
    )

    Box(contentAlignment = Alignment.BottomCenter) {
        if (menuDim > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = menuDim))
                    .pointerInput(Unit) { detectTapGestures { selectedAppForMenu = null } }
            )
        }

        Column(
            modifier = modifier
                .padding(bottom = 24.dp)
                .graphicsLayer {
                    if (menuBlur > 0.dp && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                        renderEffect = android.graphics.RenderEffect.createBlurEffect(
                            menuBlur.toPx(),
                            menuBlur.toPx(),
                            android.graphics.Shader.TileMode.CLAMP
                        ).asComposeRenderEffect()
                    }
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Power-Saving Animation Cycle (4s Rest)
            var animStep by remember { mutableStateOf(0f) }
            val arrowPulse by animateFloatAsState(
                targetValue = animStep,
                animationSpec = tween(1000, easing = FastOutSlowInEasing),
                label = "arrowPulse"
            )

            LaunchedEffect(dockState) {
                if (dockState == DockState.CLOSED) {
                    while(true) {
                        animStep = 1f
                        delay(1000)
                        animStep = 0f
                        delay(1000)
                        delay(4000) // The requested 4s rest cycle
                    }
                }
            }

            if (dockState == DockState.CLOSED) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier
                        .size(32.dp)
                        .offset(y = ((-8) * arrowPulse).dp)
                )
            }

            Box(
                modifier = Modifier
                    .height(dockHeight)
                    .width(dockWidth)
                    .graphicsLayer {
                        if (dockState == DockState.CLOSED) {
                            val s = 1f + (0.04f * arrowPulse)
                            scaleX = s
                            scaleY = s
                        }
                    }
                    .evePressable()
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onVerticalDrag = { _, dragAmount ->
                                poke()
                                if (dragAmount < -5 && dockState == DockState.CLOSED) { // Highly sensitive
                                    dockState = DockState.PEEK
                                } else if (dragAmount > 5 && dockState != DockState.CLOSED) {
                                    dockState = DockState.CLOSED
                                } else if (dragAmount < -20 && (dockState == DockState.PEEK || dockState == DockState.EXPANDED)) {
                                    onOpenApps()
                                }
                            }
                        )
                    }
                    .clickable {
                        poke()
                        dockState = if (dockState == DockState.EXPANDED) DockState.CLOSED else DockState.EXPANDED
                    },
                contentAlignment = Alignment.Center
            ) {
                EveGlassSurface(
                    modifier = Modifier.fillMaxSize(),
                    cornerRadius = 32.dp
                ) {
                    if (dockState == DockState.EXPANDED) {
                        Row(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val displayApps = if (notificationApps.isNotEmpty()) {
                                notificationApps.take(4)
                            } else {
                                dockApps.take(4)
                            }

                            displayApps.forEach { app ->
                                val hasNotification = notificationApps.any { it.packageName == app.packageName }
                                DockIcon(
                                    app = app, 
                                    hasNotification = hasNotification,
                                    onClick = { 
                                        poke()
                                        onLaunchApp(app) 
                                    },
                                    onLongClick = { 
                                        poke()
                                        haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                        selectedAppForMenu = app 
                                    }
                                )
                            }
                            DockIcon(
                                imageVector = Icons.Default.Apps,
                                onClick = {
                                    poke()
                                    onOpenApps()
                                }
                            )
                        }
                    } else {
                        // Eve's Dormant Eyes
                        val eyeColor = Color(0xFFA8FFD0)
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.graphicsLayer {
                                    alpha = eyeAlpha
                                    scaleY = eyeScaleY
                                }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(eyeColor, RoundedCornerShape(2.dp))
                                        .blur(0.5.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(eyeColor, RoundedCornerShape(2.dp))
                                        .blur(0.5.dp)
                                )
                            }

                            // Blinking Red Dot for Notifications
                            if (notificationApps.isNotEmpty()) {
                                val dotTransition = rememberInfiniteTransition(label = "dotTransition")
                                val dotAlpha by dotTransition.animateFloat(
                                    initialValue = 0.4f,
                                    targetValue = 1f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(800, easing = LinearEasing),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "dotAlpha"
                                )

                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .align(Alignment.TopEnd)
                                        .offset(x = (-12).dp, y = 12.dp)
                                        .graphicsLayer { alpha = dotAlpha }
                                        .background(Color.Red, CircleShape)
                                        .blur(1.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (selectedAppForMenu != null) {
            val app = selectedAppForMenu!!
            val appNotifs = viewModel.getNotificationsForPackage(app.packageName)
            val isInDock = dockApps.any { it.packageName == app.packageName }

            EveContextMenu(
                onDismiss = { selectedAppForMenu = null },
                title = app.label,
                notifications = appNotifs.map { sbn ->
                    val extras = sbn.notification.extras
                    NotificationPreview(
                        title = extras.getString("android.title") ?: "Alert",
                        text = extras.getCharSequence("android.text")?.toString() ?: "",
                        onClick = { 
                            try {
                                sbn.notification.contentIntent?.send()
                            } catch (e: Exception) {
                                viewModel.launchApp(app)
                            }
                        }
                    )
                },
                actions = listOfNotNull(
                    ContextAction(
                        label = if (app.isFavorite) "Remove Favorite" else "Favorite", 
                        icon = if (app.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, 
                        onClick = { viewModel.toggleFavorite(app) }
                    ),
                    if (isInDock) ContextAction("Remove from Dock", Icons.Default.Delete, { onRemoveFromDock(app) }) else null,
                    if (!isInDock) ContextAction("Add to Dock", Icons.Default.Add, { viewModel.addToDock(app) }) else null,
                    ContextAction("App Info", Icons.Default.Info, {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${app.packageName}"))
                        context.startActivity(intent)
                    })
                )
            )
        }
    }
}

@Composable
fun DockIcon(
    app: AppInfo,
    hasNotification: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .evePressable()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onLongClick() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .blur(8.dp)
                .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
        )
        
        Image(
            bitmap = app.iconBitmap,
            contentDescription = app.label,
            modifier = Modifier.size(44.dp)
        )

        if (hasNotification) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 2.dp, y = (-2).dp)
                    .background(Color.Red, CircleShape)
            )
        }
    }
}

@Composable
fun DockIcon(
    imageVector: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .evePressable()
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.size(32.dp)
        )
    }
}
