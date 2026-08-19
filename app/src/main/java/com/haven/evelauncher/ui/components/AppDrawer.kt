package com.haven.evelauncher.ui.components

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.haven.evelauncher.design.settings.LocalEveSettings
import com.haven.evelauncher.design.motion.eveBouncingIcon
import com.haven.evelauncher.design.motion.evePressable
import com.haven.evelauncher.platform.apps.AppInfo
import com.haven.evelauncher.ui.HomeViewModel
import com.haven.evelauncher.ui.theme.EveTypography
import kotlinx.coroutines.launch

import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.geometry.Offset
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint

@Composable
fun AppDrawer(
    viewModel: HomeViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings = LocalEveSettings.current
    val haptics = androidx.compose.ui.platform.LocalHapticFeedback.current
    val filteredApps by viewModel.filteredApps.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val dockApps by viewModel.dockApps.collectAsState()
    
    val gridState = rememberLazyGridState()
    val scope = rememberCoroutineScope()

    var selectedAppForMenu by remember { mutableStateOf<AppInfo?>(null) }
    
    LaunchedEffect(selectedAppForMenu) {
        viewModel.setAnyMenuOpen(selectedAppForMenu != null)
    }

    // Live Backdrop Blur State - Optimized
    val hazeState = remember { HazeState() }

    // Dynamic Dark Blur Background for Menu
    val menuDim by animateFloatAsState(
        targetValue = if (selectedAppForMenu != null) 0.65f else 0.85f,
        animationSpec = tween(200),
        label = "menuDim"
    )

    val alphabet = remember { ('A'..'Z').toList() }
    var activeLetter by remember { mutableStateOf<Char?>(null) }
    
    var atTop by remember { mutableStateOf(true) }
    var atBottom by remember { mutableStateOf(false) }

    LaunchedEffect(gridState, filteredApps) {
        snapshotFlow { 
            val firstVisible = gridState.firstVisibleItemIndex
            val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = filteredApps.size
            (firstVisible == 0 && gridState.firstVisibleItemScrollOffset == 0) to (lastVisible >= totalItems - 1)
        }.collect { (top, bottom) ->
            atTop = top
            atBottom = bottom
        }
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (atTop && available.y > 40 && source == NestedScrollSource.UserInput) {
                    onClose()
                    return Offset(0f, available.y)
                }
                if (atBottom && available.y < -40 && source == NestedScrollSource.UserInput) {
                    onClose()
                    return Offset(0f, available.y)
                }
                return Offset.Zero
            }
        }
    }
    
    val flingBehavior = ScrollableDefaults.flingBehavior()

    Box(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
            .background(Color.Black.copy(alpha = menuDim)) // Optimized: Direct background instead of full-screen glass
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 1. THE SCROLLING SOURCE
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .haze(hazeState) // Haze on the list container
            ) {
                if (filteredApps.isEmpty() && searchQuery.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 160.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        EmptySearchView(
                            query = searchQuery,
                            onPlayStore = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=$searchQuery"))
                                    context.startActivity(intent)
                                } catch (e: Exception) { e.printStackTrace() }
                            },
                            onWebSearch = {
                                try {
                                    val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                                        putExtra("query", searchQuery)
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) { e.printStackTrace() }
                            }
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        state = gridState,
                        columns = GridCells.Fixed(settings.appDrawerColumns),
                        contentPadding = PaddingValues(top = 136.dp, bottom = 120.dp, start = 36.dp, end = 52.dp), 
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize(),
                        flingBehavior = flingBehavior
                    ) {
                        itemsIndexed(
                            items = filteredApps, 
                            key = { index, app -> "${app.packageName}/${app.componentName}/$index" } 
                        ) { index, app ->
                            val isBouncing = activeLetter != null && app.label.startsWith(activeLetter!!, ignoreCase = true)
                            AppItem(
                                app = app, 
                                bounceEnabled = isBouncing,
                                staggerDelay = (index % 4) * 40,
                                onClick = { viewModel.launchApp(app) },
                                onLongClick = { 
                                    haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                    selectedAppForMenu = app 
                                }
                            )
                        }
                    }
                }
            }

            // 2. THE SEARCH BAR (Glass Bar) - Simplified for extreme smoothness
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 64.dp)
                    .padding(horizontal = 32.dp)
                    .height(56.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .hazeChild(
                        state = hazeState,
                        style = HazeStyle(
                            blurRadius = 12.dp, // Further reduced for performance
                            tint = HazeTint(Color.White.copy(alpha = 0.15f)),
                            noiseFactor = 0f
                        )
                    )
                    .drawBehind {
                        // Top-weighted light rim
                        drawRoundRect(
                            brush = Brush.linearGradient(
                                colors = listOf(Color.White.copy(alpha = 0.4f), Color.Transparent),
                                start = Offset(0f, 0f),
                                end = Offset(size.width, size.height)
                            ),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(28.dp.toPx()),
                            style = Stroke(width = 1.dp.toPx())
                        )
                    },
                contentAlignment = Alignment.CenterStart
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = EveTypography.WidgetPrimary.copy(
                        textAlign = TextAlign.Center,
                        color = Color(0xFFA8FFD0)
                    ),
                    placeholder = { 
                        Text(
                            "Search", 
                            style = EveTypography.WidgetPrimary,
                            color = Color(0xFFA8FFD0).copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        ) 
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        cursorColor = Color(0xFFA8FFD0),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color(0xFFA8FFD0),
                        unfocusedTextColor = Color(0xFFA8FFD0)
                    ),
                    singleLine = true
                )
            }

            // A-Z Slider
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(44.dp)
                    .align(Alignment.CenterEnd)
                    .padding(end = 4.dp), 
                contentAlignment = Alignment.Center
            ) {
                EveAlphabetRail(
                    alphabet = alphabet,
                    onLetterSelected = { letter: Char ->
                        activeLetter = letter
                        viewModel.onSearchQueryChanged(letter.toString())
                        if (searchQuery.isEmpty()) {
                            val index = filteredApps.indexOfFirst { it.label.startsWith(letter, ignoreCase = true) }
                            if (index != -1) {
                                scope.launch { gridState.scrollToItem(index) }
                            }
                        }
                    }
                )
            }
        }
    }
    
    // Context Menu (omitted unchanged for brevity, but kept in file)
    if (selectedAppForMenu != null) {
        val app = selectedAppForMenu!!
        val isInDock = dockApps.any { it.packageName == app.packageName }
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
            actions = listOfNotNull(
                ContextAction(
                    label = if (app.isFavorite) "Remove Favorite" else "Favorite", 
                    icon = if (app.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, 
                    onClick = { viewModel.toggleFavorite(app) }
                ),
                if (!isInDock) ContextAction("Add to Dock", Icons.Default.Add, { viewModel.addToDock(app) }) else null,
                if (isInDock) ContextAction("Remove from Dock", Icons.Default.Delete, { viewModel.removeFromDock(app) }) else null,
                ContextAction("App Info", Icons.Default.Info, {
                    try {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${app.packageName}"))
                        context.startActivity(intent)
                    } catch (e: Exception) { e.printStackTrace() }
                }),
                ContextAction("Uninstall", Icons.Default.Delete, {
                    try {
                        val intent = Intent(Intent.ACTION_DELETE, Uri.parse("package:${app.packageName}"))
                        context.startActivity(intent)
                    } catch (e: Exception) { e.printStackTrace() }
                }, isDestructive = true),
                ContextAction("Share", Icons.Default.Share, {
                    try {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "Check out ${app.label}: https://play.google.com/store/apps/details?id=${app.packageName}")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share ${app.label}"))
                    } catch (e: Exception) { e.printStackTrace() }
                })
            )
        )
    }

    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotEmpty()) {
            activeLetter = searchQuery.first().uppercaseChar()
        }
    }

    LaunchedEffect(activeLetter) {
        if (activeLetter != null) {
            kotlinx.coroutines.delay(800)
            activeLetter = null
        }
    }
}

@Composable
fun EmptySearchView(
    query: String,
    onPlayStore: () -> Unit,
    onWebSearch: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No results for \"$query\"",
            style = EveTypography.WidgetSecondary,
            color = Color.White.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        EveGlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 24.dp
        ) {
            Row(
                modifier = Modifier.padding(20.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.evePressable().clickable { onPlayStore() }) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(Brush.radialGradient(listOf(Color(0xFF00C6FF), Color(0xFF0072FF))), CircleShape)
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Shop, contentDescription = null, tint = Color.White, modifier = Modifier.fillMaxSize())
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Store", color = Color.White, style = EveTypography.Metadata, fontWeight = FontWeight.Bold)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.evePressable().clickable { onWebSearch() }) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(Brush.radialGradient(listOf(Color(0xFFFF512F), Color(0xFFDD2476))), CircleShape)
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Public, contentDescription = null, tint = Color.White, modifier = Modifier.fillMaxSize())
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Web", color = Color.White, style = EveTypography.Metadata, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AppItem(
    app: AppInfo,
    bounceEnabled: Boolean,
    staggerDelay: Int,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(4.dp)
            .graphicsLayer { 
                clip = true
            }
            .evePressable()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onLongClick() }
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.eveBouncingIcon(enabled = bounceEnabled, delay = staggerDelay)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .offset(y = 4.dp)
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                0.0f to Color.Black.copy(alpha = 0.45f),
                                0.8f to Color.Black.copy(alpha = 0.1f),
                                1.0f to Color.Transparent
                            ),
                            radius = size.width * 0.6f
                        )
                    }
            )
            
            Image(
                bitmap = app.iconBitmap,
                contentDescription = app.label,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
        }
        Text(
            text = app.label,
            style = EveTypography.AppLabel,
            color = Color.White.copy(alpha = 0.95f),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
