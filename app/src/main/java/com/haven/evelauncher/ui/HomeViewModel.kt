package com.haven.evelauncher.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.haven.evelauncher.core.permissions.EvePermissionManager
import com.haven.evelauncher.core.widget.*
import com.haven.evelauncher.data.apps.InstalledAppsDataSource
import com.haven.evelauncher.data.calendar.CalendarDataSource
import com.haven.evelauncher.data.device.BatteryDataSource
import com.haven.evelauncher.data.greeting.GreetingDataSource
import com.haven.evelauncher.data.health.HealthDataSource
import com.haven.evelauncher.data.media.MediaDataSource
import com.haven.evelauncher.data.notifications.NotificationDataSource
import com.haven.evelauncher.data.repository.EveDataRepository
import com.haven.evelauncher.data.repository.HealthRepository
import com.haven.evelauncher.data.weather.OpenMeteoWeatherRepository
import com.haven.evelauncher.data.weather.WeatherDataSource
import com.haven.evelauncher.platform.apps.AppInfo
import com.haven.evelauncher.platform.apps.LauncherService
import com.haven.evelauncher.core.context.EveContextEngine
import com.haven.evelauncher.platform.notifications.EveNotificationListenerService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import com.haven.evelauncher.data.repository.SettingsRepository
import com.haven.evelauncher.data.repository.EveSettings

class HomeViewModel(
    application: Application,
    private val launcherService: LauncherService,
    private val contextEngine: EveContextEngine
) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("eve_prefs", Context.MODE_PRIVATE)
    private val favPrefs = application.getSharedPreferences("eve_favorites", Context.MODE_PRIVATE)
    private val lastUsedPrefs = application.getSharedPreferences("eve_last_used", Context.MODE_PRIVATE)
    private val permissionManager = EvePermissionManager(application)
    
    private val settingsRepo = SettingsRepository(application)
    val settings = settingsRepo.settings

    fun updateSettings(update: (EveSettings) -> EveSettings) {
        settingsRepo.updateSettings(update)
    }

    private val locationManager = com.haven.evelauncher.data.location.EveLocationManager(application)
    private val greetingSelector = EveGreetingSelector()

    // Unified Widget Engine
    val widgetEngine = EveWidgetEngine(
        context = application,
        sources = listOf(
            WeatherDataSource(OpenMeteoWeatherRepository(), locationManager),
            HealthDataSource(HealthRepository(application)),
            BatteryDataSource(application),
            GreetingDataSource(greetingSelector),
            MediaDataSource(application),
            NotificationDataSource(application),
            CalendarDataSource(application, permissionManager),
            InstalledAppsDataSource(launcherService)
        )
    )

    private val dataRepo = EveDataRepository(application, contextEngine)
    val eveContext = contextEngine.currentContext

    private val _allApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val allApps = _allApps.asStateFlow()
    
    private val _dockApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val dockApps = _dockApps.asStateFlow()

    private val _notificationApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val notificationApps = _notificationApps.asStateFlow()

    fun getNotificationsForPackage(packageName: String): List<android.service.notification.StatusBarNotification> {
        return try {
            EveNotificationListenerService.getActiveNotifications()
                .filter { it.packageName == packageName && !it.isOngoing }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private val _isOnboardingCompleted = MutableStateFlow(prefs.getBoolean("onboarding_complete", false))
    val isOnboardingCompleted = _isOnboardingCompleted.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()
    
    private val _isAppDrawerVisible = MutableStateFlow(false)
    val isAppDrawerVisible = _isAppDrawerVisible.asStateFlow()

    private val _showDockSetup = MutableStateFlow(false)
    val showDockSetup = _showDockSetup.asStateFlow()

    private val _isSettingsVisible = MutableStateFlow(false)
    val isSettingsVisible = _isSettingsVisible.asStateFlow()

    private val _isAboutVisible = MutableStateFlow(false)
    val isAboutVisible = _isAboutVisible.asStateFlow()

    private val _isAnyMenuOpen = MutableStateFlow(false)
    val isAnyMenuOpen = _isAnyMenuOpen.asStateFlow()

    private val _supportsDarkText = MutableStateFlow(false)
    val supportsDarkText = _supportsDarkText.asStateFlow()

    val filteredApps: StateFlow<List<AppInfo>> = combine(_allApps, _searchQuery) { apps, query ->
        if (query.isEmpty()) apps else {
            apps.filter { fuzzyMatch(it.label, query) }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val prioritizedApps: StateFlow<List<AppInfo>> = filteredApps.map { apps ->
        try {
            val dialer = launcherService.getDialerApp()
            val camera = launcherService.getCameraApp()
            val chromePkg = "com.android.chrome"
            val messagesPkg = "com.google.android.apps.messaging"
            
            val topPkgs = listOfNotNull(dialer?.packageName, messagesPkg, chromePkg, camera?.packageName)
            val top = apps.filter { topPkgs.contains(it.packageName) }
            val rest = apps.filter { !topPkgs.contains(it.packageName) }.sortedBy { it.label.lowercase() }
            top + rest
        } catch (e: Exception) {
            apps.sortedBy { it.label.lowercase() }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        loadApps()
        locationManager.startLocationUpdates()
        startNotificationMonitoring()
        if (isOnboardingCompleted.value) {
            dataRepo.startSync()
        }
    }

    private fun startNotificationMonitoring() {
        viewModelScope.launch {
            while(true) {
                try {
                    if (permissionManager.hasNotificationAccess()) {
                        val activeNotifs = EveNotificationListenerService.getActiveNotifications()
                        val packages = activeNotifs
                            .filter { !it.isOngoing }
                            .map { it.packageName }
                            .distinct()
                        
                        val apps = packages.mapNotNull { pkg ->
                            _allApps.value.find { it.packageName == pkg }
                        }
                        _notificationApps.value = apps
                    } else {
                        _notificationApps.value = emptyList()
                    }
                } catch (e: Exception) {
                    _notificationApps.value = emptyList()
                }
                kotlinx.coroutines.delay(3000) 
            }
        }
    }

    fun loadApps() {
        viewModelScope.launch(Dispatchers.Default) { // Use background thread
            try {
                val apps = launcherService.getInstalledApps().map { app ->
                    val isFav = favPrefs.getBoolean(app.packageName, false)
                    val lastUsed = lastUsedPrefs.getLong(app.packageName, app.lastUsed)
                    app.copy(isFavorite = isFav, lastUsed = lastUsed)
                }
                _allApps.value = apps
                contextEngine.updateOrbit(apps)

                val savedPkgs = prefs.getString("dock_apps", "") ?: ""
                if (savedPkgs.isNotEmpty()) {
                    val resolvedDock = savedPkgs.split(",").mapNotNull { pkg ->
                        launcherService.getAppByPackage(pkg)?.let { app ->
                            val isFav = favPrefs.getBoolean(app.packageName, false)
                            app.copy(isFavorite = isFav)
                        }
                    }
                    _dockApps.value = resolvedDock
                } else if (_dockApps.value.isEmpty()) {
                    val dialer = launcherService.getDialerApp()
                    val messages = launcherService.getAppByPackage("com.google.android.apps.messaging") 
                        ?: launcherService.getAppByPackage("com.android.messaging")
                        ?: launcherService.getAppByPackage("com.samsung.android.messaging")
                    val chrome = launcherService.getAppByPackage("com.android.chrome")
                        ?: launcherService.getAppByPackage("com.google.android.browser")
                        ?: launcherService.getAppByPackage("org.mozilla.firefox")
                    val camera = launcherService.getCameraApp()
                    
                    val initialDock = listOfNotNull(dialer, messages, chrome, camera)
                    
                    val finalDock = if (initialDock.size < 4) {
                        val mostUsed = apps.sortedByDescending { it.lastUsed }
                            .filter { app -> !initialDock.any { it.packageName == app.packageName } }
                            .take(4 - initialDock.size)
                        initialDock + mostUsed
                    } else {
                        initialDock
                    }

                    saveDockToPrefs(finalDock)
                    _dockApps.value = finalDock
                }
            } catch (e: Exception) {
                // Fail gracefully
            }
        }
    }

    fun toggleFavorite(app: AppInfo) {
        val newFav = !app.isFavorite
        favPrefs.edit().putBoolean(app.packageName, newFav).apply()
        
        _allApps.update { list ->
            list.map { if (it.packageName == app.packageName) it.copy(isFavorite = newFav) else it }
        }
        _dockApps.update { list ->
            list.map { if (it.packageName == app.packageName) it.copy(isFavorite = newFav) else it }
        }
        contextEngine.updateOrbit(_allApps.value)
    }

    fun launchApp(app: AppInfo) {
        val now = System.currentTimeMillis()
        lastUsedPrefs.edit().putLong(app.packageName, now).apply()
        
        _allApps.update { list ->
            list.map { if (it.packageName == app.packageName) it.copy(lastUsed = now) else it }
        }
        
        launcherService.launchApp(app)
        setAppDrawerVisible(false)
        contextEngine.updateOrbit(_allApps.value)
    }

    fun setOnboardingCompleted(completed: Boolean) {
        _isOnboardingCompleted.value = completed
        prefs.edit().putBoolean("onboarding_complete", completed).apply()
        if (completed) {
            dataRepo.startSync()
        }
    }

    fun setDockApps(apps: List<AppInfo>) {
        _dockApps.value = apps
        saveDockToPrefs(apps)
    }

    fun addToDock(app: AppInfo) {
        if (_dockApps.value.size < 4) {
            val newList = _dockApps.value + app
            setDockApps(newList)
        } else {
            _showDockSetup.value = true
        }
    }

    fun setShowDockSetup(show: Boolean) {
        _showDockSetup.value = show
    }

    fun removeFromDock(app: AppInfo) {
        val newList = _dockApps.value.filter { it.packageName != app.packageName }
        setDockApps(newList)
    }

    private fun saveDockToPrefs(apps: List<AppInfo>) {
        val pkgs = apps.joinToString(",") { it.packageName }
        prefs.edit().putString("dock_apps", pkgs).apply()
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun setAppDrawerVisible(visible: Boolean) {
        _isAppDrawerVisible.value = visible
        if (!visible) _searchQuery.value = ""
    }

    fun setSettingsVisible(visible: Boolean) {
        _isSettingsVisible.value = visible
    }

    fun setAboutVisible(visible: Boolean) {
        _isAboutVisible.value = visible
    }

    fun setAnyMenuOpen(open: Boolean) {
        _isAnyMenuOpen.value = open
    }

    fun setSupportsDarkText(supports: Boolean) {
        _supportsDarkText.value = supports
    }

    fun isDefaultLauncher(context: Context): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
            }
            val resolveInfo = context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
            resolveInfo?.activityInfo?.packageName == context.packageName
        } catch (e: Exception) {
            false
        }
    }

    private fun fuzzyMatch(text: String, query: String): Boolean {
        if (query.isEmpty()) return true
        var textIdx = 0
        var queryIdx = 0
        val t = text.lowercase()
        val q = query.lowercase()
        while (textIdx < t.length && queryIdx < q.length) {
            if (t[textIdx] == q[queryIdx]) {
                queryIdx++
            }
            textIdx++
        }
        return queryIdx == q.length
    }
}
