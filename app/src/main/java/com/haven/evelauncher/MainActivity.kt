package com.haven.evelauncher

import android.view.WindowManager
import android.view.Surface
import android.app.WallpaperManager
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.withContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.haven.evelauncher.core.context.EveContextEngine
import com.haven.evelauncher.design.lighting.EveLightingProvider
import com.haven.evelauncher.design.motion.EveMotionManager
import com.haven.evelauncher.design.settings.EveSettingsProvider
import com.haven.evelauncher.platform.apps.LauncherService
import com.haven.evelauncher.ui.HomeViewModel
import com.haven.evelauncher.ui.screens.HomeScreen
import com.haven.evelauncher.ui.theme.EveLauncherTheme
import com.haven.evelauncher.ui.screens.OnboardingScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable High Refresh Rate support (API 30+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.attributes.preferredDisplayModeId = 0
            // Some devices need this for 90/120Hz consistency
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        
        enableEdgeToEdge()
        
        setContent {
            val context = LocalContext.current
            val motionManager = remember { EveMotionManager(context) }
            val launcherService = remember { LauncherService(context) }
            val contextEngine = remember { EveContextEngine(context) }

            // Pre-warm resources for premium readiness safely
            LaunchedEffect(Unit) {
                try {
                    withContext(kotlinx.coroutines.Dispatchers.IO) {
                        launcherService.getInstalledApps()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val homeViewModel: HomeViewModel = viewModel(
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return HomeViewModel(application, launcherService, contextEngine) as T
                    }
                }
            )

            val isAppDrawerVisible by homeViewModel.isAppDrawerVisible.collectAsState()
            val isOnboardingCompleted by homeViewModel.isOnboardingCompleted.collectAsState()

            DisposableEffect(Unit) {
                motionManager.start()
                
                // Package Change Receiver to refresh apps
                val packageReceiver = object : BroadcastReceiver() {
                    override fun onReceive(context: android.content.Context?, intent: Intent?) {
                        homeViewModel.loadApps()
                    }
                }
                val filter = IntentFilter().apply {
                    addAction(Intent.ACTION_PACKAGE_ADDED)
                    addAction(Intent.ACTION_PACKAGE_REMOVED)
                    addAction(Intent.ACTION_PACKAGE_REPLACED)
                    addDataScheme("package")
                }
                context.registerReceiver(packageReceiver, filter)

                // Wallpaper Colors Listener
                val wallpaperManager = WallpaperManager.getInstance(context)
                val listener = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                    WallpaperManager.OnColorsChangedListener { colors, _ ->
                        val hints = colors?.colorHints ?: 0
                        val supportsDarkText = (hints and 1) != 0 // HINT_SUPPORTS_DARK_TEXT is 1
                        homeViewModel.setSupportsDarkText(supportsDarkText)
                    }
                } else null

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 && listener != null) {
                    val handler = Handler(Looper.getMainLooper())
                    wallpaperManager.addOnColorsChangedListener(listener, handler)
                    
                    // Initial check
                    try {
                        val colors = wallpaperManager.getWallpaperColors(WallpaperManager.FLAG_SYSTEM)
                        val hints = colors?.colorHints ?: 0
                        homeViewModel.setSupportsDarkText((hints and 1) != 0)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                onDispose {
                    motionManager.stop()
                    try {
                        context.unregisterReceiver(packageReceiver)
                    } catch (e: Exception) { }
                    
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 && listener != null) {
                        wallpaperManager.removeOnColorsChangedListener(listener)
                    }
                }
            }

            BackHandler(enabled = isAppDrawerVisible) {
                homeViewModel.setAppDrawerVisible(false)
            }

            EveLauncherTheme {
                EveSettingsProvider(viewModel = homeViewModel) {
                    EveLightingProvider(motionManager = motionManager) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = Color.Transparent
                        ) {
                            if (isOnboardingCompleted) {
                                HomeScreen(viewModel = homeViewModel)
                                LaunchedEffect(Unit) {
                                    promptSetDefaultLauncher(context)
                                    checkNotificationAccess(context, homeViewModel)
                                }
                            } else {
                                OnboardingScreen(
                                    viewModel = homeViewModel,
                                    onComplete = { homeViewModel.setOnboardingCompleted(true) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun promptSetDefaultLauncher(context: android.content.Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val roleManager = context.getSystemService(android.content.Context.ROLE_SERVICE) as android.app.role.RoleManager
                if (roleManager.isRoleAvailable(android.app.role.RoleManager.ROLE_HOME) && 
                    !roleManager.isRoleHeld(android.app.role.RoleManager.ROLE_HOME)) {
                    val intent = roleManager.createRequestRoleIntent(android.app.role.RoleManager.ROLE_HOME)
                    startActivity(intent)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun checkNotificationAccess(context: android.content.Context, viewModel: HomeViewModel) {
        try {
            val flat = android.provider.Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
            val enabled = flat?.contains(context.packageName) == true
            if (!enabled) {
                val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
