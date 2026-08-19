package com.haven.evelauncher.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.haven.evelauncher.data.repository.BlurIntensity
import com.haven.evelauncher.design.motion.evePressable
import com.haven.evelauncher.ui.HomeViewModel
import com.haven.evelauncher.ui.components.*
import com.haven.evelauncher.ui.components.widgets.MediaWidget
import com.haven.evelauncher.ui.components.widgets.OrbitWidget
import com.haven.evelauncher.ui.components.widgets.WidgetSlot
import kotlinx.coroutines.launch
import java.util.Calendar

import com.haven.evelauncher.core.widget.*

@SuppressLint("WrongConstant")
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier
) {
    val isAppDrawerVisibleState by viewModel.isAppDrawerVisible.collectAsState()
    val eveContext by viewModel.eveContext.collectAsState()
    val widgetState by viewModel.widgetEngine.widgetState.collectAsState()
    val showDockSetup by viewModel.showDockSetup.collectAsState()
    val prioritizedApps by viewModel.prioritizedApps.collectAsState()
    val isSettingsVisible by viewModel.isSettingsVisible.collectAsState()
    val isAnyMenuOpen by viewModel.isAnyMenuOpen.collectAsState()
    val supportsDarkText by viewModel.supportsDarkText.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val context = LocalContext.current
    val haptics = androidx.compose.ui.platform.LocalHapticFeedback.current
    
    val widgetContentColor = if (supportsDarkText) Color.Black.copy(alpha = 0.8f) else Color.White

    var isDebugVisible by remember { mutableStateOf(false) }
    var showHomeMenu by remember { mutableStateOf(false) }

    LaunchedEffect(showHomeMenu) {
        viewModel.setAnyMenuOpen(showHomeMenu)
    }

    // Unified Transition Logic
    val backgroundAlpha by animateFloatAsState(
        targetValue = if (isAppDrawerVisibleState) 0f else 1f,
        animationSpec = if (settings.animationsEnabled) tween(300) else snap(),
        label = "bgAlpha"
    )

    val backgroundBlur by animateDpAsState(
        targetValue = if (showHomeMenu || isSettingsVisible || isAnyMenuOpen) {
            when(settings.blurIntensity) {
                BlurIntensity.SUBTLE -> 24.dp
                BlurIntensity.BALANCED -> 48.dp
                BlurIntensity.STRONG -> 96.dp
            }
        } else 0.dp,
        animationSpec = tween(400),
        label = "bgBlur"
    )

    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val dynamicColors = listOf(
        Color(0xFF80DEEA), Color(0xFFA5D6A7), Color(0xFFE1BEE7), 
        Color(0xFFFFF59D), Color(0xFFFFCC80), Color(0xFFEF9A9A), Color(0xFF90CAF9)
    )
    val themeColor = dynamicColors[currentHour % dynamicColors.size]

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(isAppDrawerVisibleState) {
                detectTapGestures(
                    onLongPress = {
                        if (!isAppDrawerVisibleState) {
                            haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            showHomeMenu = true
                        }
                    }
                )
            }
            .pointerInput(isAppDrawerVisibleState) {
                detectVerticalDragGestures(
                    onVerticalDrag = { change, dragAmount ->
                        if (!isAppDrawerVisibleState) {
                            if (dragAmount > 80) { // Highly responsive down swipe
                                try {
                                    val service = context.getSystemService("statusbar")
                                    val statusBarManager = Class.forName("android.app.StatusBarManager")
                                    val expandMethod = statusBarManager.getMethod("expandNotificationsPanel")
                                    expandMethod.invoke(service)
                                } catch (e: Exception) { e.printStackTrace() }
                            } else if (dragAmount < -15) { // Highly responsive up swipe
                                change.consume()
                                viewModel.setAppDrawerVisible(true)
                            }
                        }
                    }
                )
            }
    ) {

        // 1. HOME SCREEN
        Column(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = backgroundAlpha
                    if (backgroundBlur > 0.dp && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                        renderEffect = android.graphics.RenderEffect.createBlurEffect(
                            backgroundBlur.toPx(),
                            backgroundBlur.toPx(),
                            android.graphics.Shader.TileMode.CLAMP
                        ).asComposeRenderEffect()
                    }
                }
                .statusBarsPadding()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.padding(top = 40.dp))
            
            Box(modifier = Modifier.pointerInput(Unit) {
                detectTapGestures(onDoubleTap = { isDebugVisible = true })
            }) {
                EveClock(color = if (supportsDarkText) Color.Black else themeColor)
            }
            
            EveGreeting(greeting = eveContext.greeting, color = if (supportsDarkText) Color.Black else themeColor)
            
            Spacer(modifier = Modifier.padding(top = 24.dp))
            
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    WidgetSlot(state = widgetState.slot1.state, modifier = Modifier.weight(1f).aspectRatio(1f), contentColor = widgetContentColor)
                    WidgetSlot(state = widgetState.slot2.state, modifier = Modifier.weight(1f).aspectRatio(1f), contentColor = widgetContentColor)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    WidgetSlot(state = widgetState.slot3.state, modifier = Modifier.weight(1f).aspectRatio(1f), contentColor = widgetContentColor)
                    
                    // SLOT 4: Dedicated Orbit
                    Box(modifier = Modifier.weight(1f).aspectRatio(1f)) {
                        OrbitWidget(
                            viewModel = viewModel,
                            data = eveContext.orbit,
                            onLaunchApp = { viewModel.launchApp(it) },
                            modifier = Modifier.fillMaxSize().evePressable()
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            val dockApps by viewModel.dockApps.collectAsState()
            val notificationApps by viewModel.notificationApps.collectAsState()
            
            EveSmartDock(
                viewModel = viewModel,
                dockApps = dockApps,
                notificationApps = notificationApps,
                onOpenApps = { viewModel.setAppDrawerVisible(true) },
                onLaunchApp = { viewModel.launchApp(it) },
                onRemoveFromDock = { viewModel.removeFromDock(it) },
                onToggleFavorite = { viewModel.toggleFavorite(it) },
                isAppDrawerVisible = isAppDrawerVisibleState
            )
            
            EveSystemDock(
                viewModel = viewModel,
                dockApps = dockApps,
                onLaunchApp = { viewModel.launchApp(it) }
            )
        }

        // 2. APP DRAWER
        AnimatedVisibility(
            visible = isAppDrawerVisibleState,
            enter = if (settings.animationsEnabled) fadeIn() + slideInVertically(initialOffsetY = { it }) else fadeIn(snap()) + slideInVertically(snap(), initialOffsetY = { it }),
            exit = if (settings.animationsEnabled) fadeOut() + slideOutVertically(targetOffsetY = { it }) else fadeOut(snap()) + slideOutVertically(snap(), targetOffsetY = { it })
        ) {
            AppDrawer(
                viewModel = viewModel,
                onClose = { viewModel.setAppDrawerVisible(false) }
            )
        }

        AnimatedVisibility(visible = isDebugVisible, enter = fadeIn(), exit = fadeOut()) {
            EveDebugPanel(viewModel = viewModel, onClose = { isDebugVisible = false })
        }

        // 4. DOCK SETUP OVERLAY
        AnimatedVisibility(
            visible = showDockSetup,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.9f))) {
                DockSetupStep(prioritizedApps) { selectedApps ->
                    viewModel.setDockApps(selectedApps)
                    viewModel.setShowDockSetup(false)
                }
                
                IconButton(
                    onClick = { viewModel.setShowDockSetup(false) },
                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        }

        // 5. HOME LONG PRESS MENU
        if (showHomeMenu) {
            EveContextMenu(
                onDismiss = { showHomeMenu = false },
                actions = listOf(
                    ContextAction(
                        label = "Settings",
                        icon = Icons.Default.Settings,
                        onClick = { viewModel.setSettingsVisible(true) }
                    ),
                    ContextAction(
                        label = "Wallpaper",
                        icon = Icons.Default.Image,
                        onClick = {
                            val intent = Intent(Intent.ACTION_SET_WALLPAPER)
                            context.startActivity(Intent.createChooser(intent, "Set Wallpaper"))
                        }
                    )
                )
            )
        }

        // 6. SETTINGS OVERLAY
        AnimatedVisibility(
            visible = isSettingsVisible,
            enter = if (settings.animationsEnabled) fadeIn() + slideInVertically(initialOffsetY = { it }) else fadeIn(snap()),
            exit = if (settings.animationsEnabled) fadeOut() + slideOutVertically(targetOffsetY = { it }) else fadeOut(snap())
        ) {
            SettingsScreen(
                viewModel = viewModel,
                onClose = { viewModel.setSettingsVisible(false) }
            )
        }
    }
}
