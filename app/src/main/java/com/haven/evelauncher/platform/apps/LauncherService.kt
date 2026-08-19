package com.haven.evelauncher.platform.apps

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap

data class AppInfo(
    val label: String,
    val packageName: String,
    val componentName: String, // Fully qualified activity name
    val icon: Drawable,
    val iconBitmap: ImageBitmap,
    val launchIntent: Intent?,
    val lastUsed: Long = 0L,
    val isFavorite: Boolean = false
)

class LauncherService(private val context: Context) {
    // Memory-efficient Icon Cache (Premium approach)
    private val iconCache = LruCache<String, ImageBitmap>(100)

    fun getInstalledApps(): List<AppInfo> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        
        return try {
            pm.queryIntentActivities(intent, 0).mapNotNull { resolveInfo ->
                try {
                    val pkg = resolveInfo.activityInfo.packageName
                    val cls = resolveInfo.activityInfo.name
                    val icon = resolveInfo.loadIcon(pm)
                    
                    val cacheKey = "$pkg/$cls"
                    var bitmap = iconCache.get(cacheKey)
                    if (bitmap == null) {
                        bitmap = icon.toBitmap(256, 256, android.graphics.Bitmap.Config.ARGB_8888).asImageBitmap()
                        iconCache.put(cacheKey, bitmap)
                    }

                    AppInfo(
                        label = resolveInfo.loadLabel(pm).toString(),
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

    fun getAppByPackage(packageName: String): AppInfo? {
        val pm = context.packageManager
        return try {
            val appInfo = pm.getApplicationInfo(packageName, 0)
            val launchIntent = pm.getLaunchIntentForPackage(packageName)
            val icon = pm.getApplicationIcon(appInfo)
            
            val componentName = launchIntent?.component?.className ?: ""
            val bitmap = getIconBitmap(packageName + componentName, icon)
            
            AppInfo(
                label = pm.getApplicationLabel(appInfo).toString(),
                packageName = packageName,
                componentName = componentName,
                icon = icon,
                iconBitmap = bitmap,
                launchIntent = launchIntent,
                lastUsed = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            null
        }
    }

    fun getDialerApp(): AppInfo? {
        return try {
            val intent = Intent(Intent.ACTION_DIAL)
            resolveFirstApp(intent)
        } catch (e: Exception) {
            null
        }
    }

    fun getCameraApp(): AppInfo? {
        return try {
            val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
            resolveFirstApp(intent)
        } catch (e: Exception) {
            null
        }
    }

    private fun resolveFirstApp(intent: Intent): AppInfo? {
        val pm = context.packageManager
        val resolveInfo = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return resolveInfo?.activityInfo?.packageName?.let { getAppByPackage(it) }
    }

    private fun getIconBitmap(packageName: String, icon: Drawable): ImageBitmap {
        var bitmap = iconCache.get(packageName)
        if (bitmap == null) {
            bitmap = icon.toBitmap(256, 256, android.graphics.Bitmap.Config.ARGB_8888).asImageBitmap()
            iconCache.put(packageName, bitmap)
        }
        return bitmap!!
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
