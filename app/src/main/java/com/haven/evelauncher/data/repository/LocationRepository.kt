package com.haven.evelauncher.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

data class EveLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val timestamp: Long = System.currentTimeMillis()
)

class LocationRepository(private val context: Context) {
    private val client = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    fun getLocationUpdates(): Flow<EveLocation?> = callbackFlow {
        val request = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 600000) // 10 min
            .setMinUpdateIntervalMillis(300000) // 5 min
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let {
                    trySend(EveLocation(it.latitude, it.longitude, it.accuracy))
                }
            }
        }

        try {
            client.requestLocationUpdates(request, callback, Looper.getMainLooper())
            
            // Immediate last location check
            client.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    trySend(EveLocation(it.latitude, it.longitude, it.accuracy))
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
            trySend(null)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        awaitClose {
            try {
                client.removeLocationUpdates(callback)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }.distinctUntilChanged()
}
