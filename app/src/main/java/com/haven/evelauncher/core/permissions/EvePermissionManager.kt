package com.haven.evelauncher.core.permissions

import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.core.content.ContextCompat

enum class EvePermission(val androidPermission: String) {
    CALENDAR(android.Manifest.permission.READ_CALENDAR),
    LOCATION(android.Manifest.permission.ACCESS_FINE_LOCATION)
}

class EvePermissionManager(private val context: Context) {
    fun isGranted(permission: EvePermission): Boolean {
        return ContextCompat.checkSelfPermission(context, permission.androidPermission) == PackageManager.PERMISSION_GRANTED
    }

    fun hasNotificationAccess(): Boolean {
        val pkgName = context.packageName
        val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        return flat?.contains(pkgName) == true
    }
}
