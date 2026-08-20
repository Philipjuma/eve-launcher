package com.haven.evelauncher.platform.apps

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class AppInfo(
    val label: String,
    val packageName: String,
    val componentName: String, // Fully qualified activity name
    val icon: Drawable,
    val iconBitmap: ImageBitmap,
    val lastUsed: Long = 0L,
    val isFavorite: Boolean = false,
    val launchIntent: Intent? = null
)

class LauncherService(private val context: Context) {
    // Memory-efficient Icon Cache for Rendered Bitmaps
    // Increased to 500 to cover almost all user apps without re-rendering
    private val iconCache = LruCache<String, ImageBitmap>(500)

    suspend fun getInstalledApps(): List<AppInfo> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        
        try {
            pm.queryIntentActivities(intent, 0).mapNotNull { resolveInfo ->
                try {
                    val pkg = resolveInfo.activityInfo.packageName
                    val cls = resolveInfo.activityInfo.name
                    val cacheKey = "$pkg/$cls"
                    
                    var bitmap = iconCache.get(cacheKey)
                    val label = resolveInfo.loadLabel(pm).toString()
                    val icon = resolveInfo.loadIcon(pm)
                    
                    if (bitmap == null) {
                        // Render vector/drawable to bitmap once and cache
                        // 160x160 is plenty for app icons, saves memory over 256x256
                        bitmap = icon.toBitmap(160, 160, android.graphics.Bitmap.Config.ARGB_8888).asImageBitmap()
                        iconCache.put(cacheKey, bitmap)
                    }

                    AppInfo(
                        label = label,
                        packageName = pkg,
                        componentName = cls,
                        icon = icon,
                        iconBitmap = bitmap!!,
                        launchIntent = pm.getLaunchIntentForPackage(pkg),
                        lastUsed = System.currentTimeMillis() - (Math.random() * 10000000).toLong()
                    )
                } catch (e: Exception) {
                    null
                }
            }.sortedBy { it.label.lowercase() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getAppByPackage(packageName: String): AppInfo? = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        try {
            val appInfo = pm.getApplicationInfo(packageName, 0)
            val launchIntent = pm.getLaunchIntentForPackage(packageName)
            val icon = pm.getApplicationIcon(appInfo)
            
            val componentName = launchIntent?.component?.className ?: ""
            val cacheKey = "$packageName/$componentName"
            
            var bitmap = iconCache.get(cacheKey)
            if (bitmap == null) {
                bitmap = icon.toBitmap(160, 160, android.graphics.Bitmap.Config.ARGB_8888).asImageBitmap()
                iconCache.put(cacheKey, bitmap)
            }
            
            AppInfo(
                label = pm.getApplicationLabel(appInfo).toString(),
                packageName = packageName,
                componentName = componentName,
                icon = icon,
                iconBitmap = bitmap!!,
                launchIntent = launchIntent,
                lastUsed = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getDialerApp(): AppInfo? = withContext(Dispatchers.IO) {
        val intent = Intent(Intent.ACTION_DIAL)
        resolveFirstApp(intent)
    }

    suspend fun getCameraApp(): AppInfo? = withContext(Dispatchers.IO) {
        val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
        resolveFirstApp(intent)
    }

    private suspend fun resolveFirstApp(intent: Intent): AppInfo? {
        val pm = context.packageManager
        val resolveInfo = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return resolveInfo?.activityInfo?.packageName?.let { getAppByPackage(it) }
    }

    fun launchApp(app: AppInfo) {
        try {
            app.launchIntent?.let {
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
