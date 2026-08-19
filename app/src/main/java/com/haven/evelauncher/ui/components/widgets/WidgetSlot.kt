package com.haven.evelauncher.ui.components.widgets

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.haven.evelauncher.core.widget.*
import com.haven.evelauncher.ui.components.EveGlassCard
import com.haven.evelauncher.design.motion.evePressable
import com.haven.evelauncher.ui.theme.EveTypography
import kotlinx.coroutines.delay

@Composable
fun WidgetSlot(
    state: WidgetSlotState,
    modifier: Modifier = Modifier,
    contentColor: Color = Color.White
) {
    val context = LocalContext.current

    // Detect if this slot should be a Media Widget
    val musicItem = if (state is WidgetSlotState.Content) {
        state.items.find { it.sourceType == EveDataSourceType.MUSIC }
    } else null

    if (musicItem != null) {
        MediaWidget(
            item = musicItem,
            modifier = modifier,
            contentColor = contentColor
        )
    } else {
        EveGlassCard(
            modifier = modifier
                .evePressable()
                .clickable {
                    when (state) {
                        is WidgetSlotState.Content -> {
                            state.items.firstOrNull()?.let { 
                                EveActionExecutor.execute(context, it) 
                            }
                        }
                        else -> {}
                    }
                }
        ) {
            AnimatedContent(
                targetState = state,
                transitionSpec = {
                    fadeIn(animationSpec = tween(250)) + scaleIn(initialScale = 0.96f) togetherWith
                    fadeOut(animationSpec = tween(250))
                },
                label = "widgetSlotTransition"
            ) { targetState ->
                Box(
                    modifier = Modifier.fillMaxSize().padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when (targetState) {
                        is WidgetSlotState.Loading -> {
                            Text("...", style = EveTypography.Metadata, color = contentColor.copy(alpha = 0.2f))
                        }
                        is WidgetSlotState.Empty -> {
                            Text("HONESTY", style = EveTypography.Metadata.copy(letterSpacing = 2.sp), color = contentColor.copy(alpha = 0.1f))
                        }
                        is WidgetSlotState.Content -> {
                            val notifications = targetState.items.filter { it.sourceType == EveDataSourceType.NOTIFICATION }
                            
                            if (notifications.isNotEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopStart) {
                                    NotificationStack(notifications, contentColor)
                                }
                            } else {
                                Column(
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxHeight()
                                ) {
                                    val itemsToShow = targetState.items.take(3)
                                    itemsToShow.forEach { item ->
                                        CompactCandidateItem(
                                            item = item, 
                                            contentColor = contentColor,
                                            isSolo = itemsToShow.size == 1
                                        )
                                    }
                                }
                            }
                        }
                        is WidgetSlotState.Error -> {
                            Text("WAITING", style = EveTypography.Metadata, color = contentColor.copy(alpha = 0.1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationStack(items: List<EveWidgetCandidate>, contentColor: Color) {
    val listState = rememberLazyListState()
    
    if (items.size > 1) {
        LaunchedEffect(items) {
            while(true) {
                delay(4000)
                if (listState.layoutInfo.visibleItemsInfo.isNotEmpty()) {
                    val lastIndex = listState.layoutInfo.totalItemsCount - 1
                    val nextIndex = if (listState.firstVisibleItemIndex < lastIndex) listState.firstVisibleItemIndex + 1 else 0
                    listState.animateScrollToItem(nextIndex)
                }
            }
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(items) { index, item ->
            LargeNotificationItem(item, contentColor)
            if (index < items.size - 1) {
                HorizontalDivider(
                    modifier = Modifier.padding(top = 8.dp),
                    thickness = 0.5.dp,
                    color = contentColor.copy(alpha = 0.1f)
                )
            }
        }
    }
}

@Composable
private fun LargeNotificationItem(item: EveWidgetCandidate, contentColor: Color) {
    val context = LocalContext.current
    val appIcon = remember(item.icon?.vectorName) {
        try {
            item.icon?.vectorName?.let { pkg ->
                context.packageManager.getApplicationIcon(pkg).toBitmap(100, 100).asImageBitmap()
            }
        } catch (e: Exception) { null }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (appIcon != null) {
                Image(
                    bitmap = appIcon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Text(
                text = item.title,
                style = EveTypography.WidgetHeadline.copy(color = contentColor.copy(alpha = 0.9f)),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        Spacer(Modifier.height(2.dp))
        
        item.subtitle?.let {
            Text(
                text = it,
                style = EveTypography.WidgetSecondary.copy(
                    color = contentColor.copy(alpha = 0.7f),
                    lineHeight = 18.sp
                ),
                maxLines = 8, 
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CompactCandidateItem(
    item: EveWidgetCandidate, 
    contentColor: Color,
    isSolo: Boolean = false
) {
    val isActivity = item.sourceType == EveDataSourceType.HEALTH
    val isWeather = item.sourceType == EveDataSourceType.WEATHER
    
    val horizontalArrangement = if (isSolo) Arrangement.Start else Arrangement.Center
    val alignment = if (isSolo) Alignment.Start else Alignment.CenterHorizontally

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = horizontalArrangement,
        modifier = Modifier.fillMaxWidth()
    ) {
        item.icon?.let { icon ->
            if (icon.emoji != null) {
                Text(
                    text = icon.emoji,
                    fontSize = if (isWeather) 48.sp else if (isActivity) 28.sp else 24.sp,
                    modifier = Modifier.width(if (isWeather) 56.dp else if (isActivity) 32.dp else 26.dp)
                )
            }
        }
        
        Column(horizontalAlignment = if (isWeather || isActivity) Alignment.Start else alignment) {
            Text(
                text = item.title,
                style = if (isActivity) EveTypography.Clock.copy(fontSize = 36.sp) else EveTypography.WidgetPrimary,
                color = contentColor,
                maxLines = 2, 
                overflow = TextOverflow.Ellipsis,
                textAlign = if (isSolo) TextAlign.Start else TextAlign.Center
            )
            item.subtitle?.let { subtitle ->
                Text(
                    text = subtitle,
                    style = EveTypography.WidgetSecondary,
                    color = contentColor.copy(alpha = 0.7f),
                    maxLines = if (isSolo) 6 else 1, 
                    overflow = TextOverflow.Ellipsis,
                    textAlign = if (isSolo) TextAlign.Start else TextAlign.Center
                )
            }
        }
    }
}
