package com.haven.evelauncher.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haven.evelauncher.R
import com.haven.evelauncher.data.repository.BlurIntensity
import com.haven.evelauncher.data.repository.ClockSize
import com.haven.evelauncher.ui.HomeViewModel
import com.haven.evelauncher.ui.components.EveGlassCard
import com.haven.evelauncher.ui.theme.EveTypography
import java.util.Locale

@Composable
fun SettingsScreen(
    viewModel: HomeViewModel,
    onClose: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()
    val isAboutVisible by viewModel.isAboutVisible.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f))
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp).padding(4.dp)
                )

                Column(modifier = Modifier.padding(start = 8.dp)) {
                    Text(
                        text = "EVE",
                        style = EveTypography.OnboardingTitle.copy(fontSize = 26.sp, fontWeight = FontWeight.SemiBold), // Clearer EVE header
                        color = Color(0xFFA8FFD0)
                    )
                    Text(
                        text = "by PJ10 INDUSTRIES™",
                        style = EveTypography.Metadata.copy(fontSize = 11.sp, letterSpacing = 1.2.sp, fontWeight = FontWeight.Medium), // Clearer sub-header
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Appearance Section
            SettingsSectionTitle("Appearance")
            
            EveGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Clock Settings
                    SettingsItemLabel("Clock Size")
                    SegmentedSelector(
                        options = remember { ClockSize.entries.toList() },
                        selected = settings.clockSize,
                        onSelected = { size -> viewModel.updateSettings { it.copy(clockSize = size) } }
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    SettingsItemLabel("Clock Color")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FilterChip(
                            selected = settings.isClockDynamic,
                            onClick = { viewModel.updateSettings { it.copy(isClockDynamic = true) } },
                            label = { Text("Dynamic") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFA8FFD0).copy(alpha = 0.2f),
                                selectedLabelColor = Color(0xFFA8FFD0)
                            )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        FilterChip(
                            selected = !settings.isClockDynamic,
                            onClick = { viewModel.updateSettings { it.copy(isClockDynamic = false) } },
                            label = { Text("Personal") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFA8FFD0).copy(alpha = 0.2f),
                                selectedLabelColor = Color(0xFFA8FFD0)
                            )
                        )
                    }
                    
                    if (!settings.isClockDynamic) {
                        Spacer(modifier = Modifier.height(12.dp))
                        ColorBar(
                            selectedColor = Color(settings.personalClockColor),
                            onColorSelected = { color -> viewModel.updateSettings { s -> s.copy(personalClockColor = color.toArgb()) } }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    SettingsItemLabel("Blur Intensity")
                    SegmentedSelector(
                        options = remember { BlurIntensity.entries.toList() },
                        selected = settings.blurIntensity,
                        onSelected = { intensity -> viewModel.updateSettings { it.copy(blurIntensity = intensity) } }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Animations Section
            SettingsSectionTitle("Animations")
            EveGlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Enable Animations", style = EveTypography.WidgetSecondary.copy(fontWeight = FontWeight.Medium), color = Color.White)
                    Switch(
                        checked = settings.animationsEnabled,
                        onCheckedChange = { enabled ->
                            viewModel.updateSettings { it.copy(animationsEnabled = enabled) }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFFA8FFD0),
                            checkedTrackColor = Color(0xFFA8FFD0).copy(alpha = 0.3f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // App Drawer Grid
            SettingsSectionTitle("App Drawer Grid")
            EveGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val gridOptions = remember { listOf(4 to 5, 4 to 6, 5 to 5, 5 to 6, 5 to 7, 6 to 6) }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Just taking first 3 for this row to avoid FlowRow complexities if not available
                        gridOptions.take(3).forEach { (cols, rows) ->
                            val isSelected = settings.appDrawerColumns == cols && settings.appDrawerRows == rows
                            FilterChip(
                                selected = isSelected,
                                onClick = { 
                                    viewModel.updateSettings { it.copy(appDrawerColumns = cols, appDrawerRows = rows) } 
                                },
                                label = { Text("$cols × $rows") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFA8FFD0).copy(alpha = 0.2f),
                                    selectedLabelColor = Color(0xFFA8FFD0)
                                )
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        gridOptions.drop(3).forEach { (cols, rows) ->
                            val isSelected = settings.appDrawerColumns == cols && settings.appDrawerRows == rows
                            FilterChip(
                                selected = isSelected,
                                onClick = { 
                                    viewModel.updateSettings { it.copy(appDrawerColumns = cols, appDrawerRows = rows) } 
                                },
                                label = { Text("$cols × $rows") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFA8FFD0).copy(alpha = 0.2f),
                                    selectedLabelColor = Color(0xFFA8FFD0)
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Default Launcher
            val context = LocalContext.current
            val isDefault = viewModel.isDefaultLauncher(context)
            SettingsActionItem(
                title = if (isDefault) "Eve is your default launcher" else "Set Eve as default launcher",
                icon = if (isDefault) Icons.Default.CheckCircle else Icons.Default.Home,
                onClick = {
                    try {
                        val intent = Intent(Settings.ACTION_HOME_SETTINGS)
                        context.startActivity(intent)
                    } catch (e: Exception) { e.printStackTrace() }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // About
            SettingsActionItem(
                title = "About Eve Launcher",
                icon = Icons.Default.Info,
                onClick = { viewModel.setAboutVisible(true) }
            )

            Spacer(modifier = Modifier.height(64.dp))
        }

        // About Overlay
        AnimatedVisibility(
            visible = isAboutVisible,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
        ) {
            AboutScreen(onClose = { viewModel.setAboutVisible(false) })
        }
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = EveTypography.Metadata.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 1.2.sp, fontSize = 12.sp), // Clearer Section Title
        color = Color.White.copy(alpha = 0.6f),
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
fun SettingsItemLabel(label: String) {
    Text(
        text = label,
        style = EveTypography.WidgetSecondary.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold), // Clearer Item Label
        color = Color.White.copy(alpha = 0.85f),
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun <T : Enum<T>> SegmentedSelector(
    options: List<T>,
    selected: T,
    onSelected: (T) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.05f))
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) Color(0xFFA8FFD0).copy(alpha = 0.2f) else Color.Transparent)
                    .clickable { onSelected(option) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = remember(option) { option.name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() } },
                    style = EveTypography.Metadata.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium),
                    color = if (isSelected) Color(0xFFA8FFD0) else Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun ColorBar(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit
) {
    val colors = remember {
        listOf(
            Color.White, Color(0xFFA8FFD0), Color(0xFF80DEEA), Color(0xFFA5D6A7), 
            Color(0xFFFFF59D), Color(0xFFFFCC80), Color(0xFFEF9A9A), Color(0xFF90CAF9)
        )
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        colors.forEach { color ->
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color)
                    .clickable { onColorSelected(color) }
            ) {
                if (selectedColor == color) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsActionItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    EveGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = Color.White.copy(alpha = 0.8f))
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, style = EveTypography.WidgetSecondary.copy(fontWeight = FontWeight.Medium), color = Color.White)
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White.copy(alpha = 0.4f))
        }
    }
}

@Composable
fun AboutScreen(onClose: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.95f))
            .statusBarsPadding()
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconButton(onClick = onClose, modifier = Modifier.align(Alignment.Start)) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            Text(
                text = "Eve Launcher",
                style = EveTypography.OnboardingTitle.copy(fontSize = 32.sp, fontWeight = FontWeight.Bold),
                color = Color(0xFFA8FFD0)
            )
            Text(
                text = "V 1.0.0",
                style = EveTypography.Metadata.copy(fontWeight = FontWeight.Medium),
                color = Color.White.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Text(
                text = "Eve is an intelligent companion living inside your phone, built to feel present and honest, never like it's faking it.\n\nA project by Sir Philip Juma, founder of PJ10 Industries™, an Android enthusiast. Prefers the outdoors to most screens, which is a strange place to be building one.",
                style = EveTypography.WidgetSecondary.copy(lineHeight = 24.sp),
                textAlign = TextAlign.Center,
                color = Color.White.copy(alpha = 0.85f)
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Text(
                text = "CREDITS",
                style = EveTypography.Metadata.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp),
                color = Color.White.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "This product uses the TMDB API but is not endorsed or certified by TMDB.\n\nWeather data by Open-Meteo.\nFootball data by football-data.org.\nHoliday data by Nager.Date.",
                style = EveTypography.Metadata.copy(lineHeight = 16.sp, fontWeight = FontWeight.Medium),
                textAlign = TextAlign.Center,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}
