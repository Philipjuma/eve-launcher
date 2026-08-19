package com.haven.evelauncher.ui.components

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.haven.evelauncher.design.motion.evePressable
import com.haven.evelauncher.platform.apps.AppInfo
import com.haven.evelauncher.ui.HomeViewModel

@Composable
fun EveSystemDock(
    viewModel: HomeViewModel,
    dockApps: List<AppInfo>,
    onLaunchApp: (AppInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedAppForMenu by remember { mutableStateOf<AppInfo?>(null) }
    val context = LocalContext.current
    val haptics = androidx.compose.ui.platform.LocalHapticFeedback.current

    LaunchedEffect(selectedAppForMenu) {
        viewModel.setAnyMenuOpen(selectedAppForMenu != null)
    }

    EveGlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp) // Tighter to edges
            .padding(bottom = 12.dp) // Bottom anchored
            .height(100.dp), // Slightly taller for more presence
        cornerRadius = 36.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround, // More even spacing
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Priority resolution of the specific 4 apps
            val dialerPkg = listOf("com.google.android.dialer", "com.android.dialer", "com.samsung.android.dialer")
            val messagesPkg = listOf("com.google.android.apps.messaging", "com.android.messaging", "com.samsung.android.messaging")
            val chromePkg = listOf("com.android.chrome", "com.google.android.browser", "org.mozilla.firefox")
            val cameraPkg = listOf("com.google.android.GoogleCamera", "com.android.camera", "com.android.camera2", "com.sec.android.app.camera")

            val targetList = listOf(dialerPkg, messagesPkg, chromePkg, cameraPkg)
            val displayedPackages = mutableSetOf<String>()

            targetList.forEach { variants ->
                val app = dockApps.find { variants.contains(it.packageName) }
                if (app != null && !displayedPackages.contains(app.packageName)) {
                    SystemDockIcon(
                        app = app, 
                        onClick = { onLaunchApp(app) },
                        onLongClick = { 
                            haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            selectedAppForMenu = app 
                        }
                    )
                    displayedPackages.add(app.packageName)
                }
            }

            // Fill remaining space if dockApps had fallbacks
            if (displayedPackages.size < dockApps.size) {
                dockApps.forEach { app ->
                    if (!displayedPackages.contains(app.packageName) && displayedPackages.size < 4) {
                        SystemDockIcon(
                            app = app,
                            onClick = { onLaunchApp(app) },
                            onLongClick = { selectedAppForMenu = app }
                        )
                        displayedPackages.add(app.packageName)
                    }
                }
            }
        }

        if (selectedAppForMenu != null) {
            val app = selectedAppForMenu!!
            val appNotifs = viewModel.getNotificationsForPackage(app.packageName)
            
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
                actions = listOf(
                    ContextAction(
                        label = if (app.isFavorite) "Remove Favorite" else "Favorite", 
                        icon = if (app.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, 
                        onClick = { viewModel.toggleFavorite(app) }
                    ),
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
fun SystemDockIcon(
    app: AppInfo,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(72.dp) // Increased touch target
            .evePressable()
            .pointerInput(app) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onLongClick() }
                )
            }
    ) {
        // Subtle background glow/shadow to lift the icon
        Box(
            modifier = Modifier
                .size(56.dp)
                .blur(12.dp)
                .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
        )
        
        Image(
            bitmap = app.iconBitmap,
            contentDescription = app.label,
            modifier = Modifier.size(60.dp) // Real system icon size
        )
    }
}
