package com.haven.evelauncher.ui.components.widgets

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.haven.evelauncher.core.widget.OrbitData
import com.haven.evelauncher.platform.apps.AppInfo
import com.haven.evelauncher.ui.HomeViewModel
import com.haven.evelauncher.ui.components.*

@Composable
fun OrbitWidget(
    viewModel: HomeViewModel,
    data: OrbitData,
    onLaunchApp: (AppInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedAppForMenu by remember { mutableStateOf<AppInfo?>(null) }
    val context = LocalContext.current

    LaunchedEffect(selectedAppForMenu) {
        viewModel.setAnyMenuOpen(selectedAppForMenu != null)
    }

    EveGlassCard(modifier = modifier) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize().padding(8.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            itemsIndexed(
                items = data.predictiveApps.take(9),
                key = { index, app -> "${app.packageName}/$index" }
            ) { _, app ->
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .pointerInput(app) {
                            detectTapGestures(
                                onTap = { onLaunchApp(app) },
                                onLongPress = { selectedAppForMenu = app }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Pronounced Square Shadow - Physically Centered
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .blur(10.dp)
                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    )
                    
                    Image(
                        bitmap = app.iconBitmap,
                        contentDescription = app.label,
                        modifier = Modifier.size(48.dp)
                    )
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
                        try {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${app.packageName}"))
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    })
                )
            )
        }
    }
}
