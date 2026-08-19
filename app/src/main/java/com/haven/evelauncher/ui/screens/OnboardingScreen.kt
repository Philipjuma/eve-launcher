package com.haven.evelauncher.ui.screens

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haven.evelauncher.ui.HomeViewModel
import com.haven.evelauncher.ui.components.DockSetupStep
import com.haven.evelauncher.ui.components.EveGlassCard
import com.haven.evelauncher.ui.components.PermissionItem
import com.haven.evelauncher.ui.theme.EveTypography

enum class OnboardingStep { WELCOME, PERMISSIONS, DOCK_SETUP }

@Composable
fun OnboardingScreen(
    viewModel: HomeViewModel,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentStep by remember { mutableStateOf(OnboardingStep.WELCOME) }
    val prioritizedApps by viewModel.prioritizedApps.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        when (currentStep) {
            OnboardingStep.WELCOME -> WelcomeStep { currentStep = OnboardingStep.PERMISSIONS }
            OnboardingStep.PERMISSIONS -> PermissionsStep { currentStep = OnboardingStep.DOCK_SETUP }
            OnboardingStep.DOCK_SETUP -> DockSetupStep(prioritizedApps) { selectedApps ->
                viewModel.setDockApps(selectedApps)
                onComplete()
            }
        }
    }
}

@Composable
fun WelcomeStep(onNext: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome to Eve Launcher...",
            style = EveTypography.OnboardingTitle,
            color = Color(0xFFA8FFD0)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "by PJ10 INDUSTRIES™",
            style = EveTypography.Metadata.copy(letterSpacing = 2.sp),
            color = Color.White.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onNext, 
            modifier = Modifier.height(56.dp).fillMaxWidth(0.6f),
            shape = MaterialTheme.shapes.extraLarge,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White.copy(alpha = 0.1f),
                contentColor = Color.White
            )
        ) {
            Text("Begin Experience", style = EveTypography.WidgetSecondary)
        }
    }
}

@Composable
fun PermissionsStep(onNext: () -> Unit) {
    val context = LocalContext.current
    var showNotifInfo by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        // After system perms, check notification access (special settings screen)
        val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        val enabled = flat?.contains(context.packageName) == true
        if (!enabled) {
            showNotifInfo = true
        } else {
            onNext()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Foundations",
                style = EveTypography.OnboardingTitle.copy(fontSize = 32.sp), // Reduced from 36.sp
                color = Color(0xFFA8FFD0)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Eve requires your context to breathe.",
                style = EveTypography.WidgetSecondary, // Not bold, not that big
                textAlign = TextAlign.Center,
                color = Color.White.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(32.dp))
            
            EveGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(24.dp)) {
                    PermissionItem("Location", "For dynamic atmosphere and local events.")
                    Spacer(modifier = Modifier.height(12.dp))
                    PermissionItem("Activity", "To sense your physical motion.")
                    Spacer(modifier = Modifier.height(12.dp))
                    PermissionItem("Calendar", "To keep you aware of your schedule.")
                    Spacer(modifier = Modifier.height(12.dp))
                    PermissionItem("Notifications", "For the Haven Engine intelligence.")
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
            Button(
                onClick = {
                    val perms = mutableListOf(
                        Manifest.permission.ACCESS_FINE_LOCATION, 
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.READ_CALENDAR
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) perms.add(Manifest.permission.ACTIVITY_RECOGNITION)
                    permissionLauncher.launch(perms.toTypedArray())
                },
                modifier = Modifier.height(56.dp).fillMaxWidth(0.6f),
                shape = MaterialTheme.shapes.extraLarge,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.1f),
                    contentColor = Color.White
                )
            ) {
                Text("Grant Access", style = EveTypography.WidgetSecondary)
            }
        }

        // Instructional Glassy Popup for Notification Access
        AnimatedVisibility(
            visible = showNotifInfo,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                EveGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Info, 
                            contentDescription = null, 
                            tint = Color(0xFFA8FFD0),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Notification Access", 
                            style = EveTypography.WidgetHeadline, 
                            color = Color.White
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Eve needs to see alerts to coordinate your widgets.\n\nOn the next screen, find 'Eve Launcher' and toggle it ON.",
                            style = EveTypography.WidgetSecondary,
                            textAlign = TextAlign.Center,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Spacer(Modifier.height(32.dp))
                        Button(
                            onClick = {
                                val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                                showNotifInfo = false
                                // We can't detect when they come back easily here without a lifecycle listener, 
                                // but we'll assume they grant it or we'll ask again if needed.
                                onNext() 
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.large
                        ) {
                            Text("I Understand", style = EveTypography.WidgetPrimary)
                        }
                    }
                }
            }
        }
    }
}
