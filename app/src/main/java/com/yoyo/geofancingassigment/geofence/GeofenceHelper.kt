package com.yoyo.geofancingassigment.geofence

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.yoyo.geofancingassigment.recievers.GeofenceReceiver
import com.yoyo.geofancingassigment.database.DatabaseHelper
import com.yoyo.geofancingassigment.database.models.GeoInfo
import com.yoyo.geofancingassigment.utils.SingletonHolder
import com.yoyo.geofancingassigment.utils.getFormattedDateTime
import java.lang.Exception
import com.yoyo.geofancingassigment.R
import com.yoyo.geofancingassigment.services.GeofencingForegroundService
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class GeofenceHelper private constructor(private val context: Context) {
    companion object : SingletonHolder<GeofenceHelper, Context>(::GeofenceHelper) {
        private const val DWELL_THRESHOLD: Int = 3000
    }

    private var pendingIntent: PendingIntent? = null

    private fun getGeofenceRequest(geofence: Geofence): GeofencingRequest {
        return GeofencingRequest.Builder()
            .addGeofence(geofence)
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_EXIT)
            .build()
    }

    private fun getGeofence(id: String, latLng: LatLng, radius: Float, transitionTypes: Int): Geofence {
        return Geofence.Builder()
            .setCircularRegion(latLng.latitude, latLng.longitude, radius)
            .setRequestId(id)
            .setTransitionTypes(transitionTypes)
            .setLoiteringDelay(DWELL_THRESHOLD)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .build()
    }

    private fun getPendingIntent(): PendingIntent {
        if (pendingIntent != null) {
            return pendingIntent as PendingIntent
        }
        val intent = Intent(context, GeofencingForegroundService::class.java)
        pendingIntent = PendingIntent.getForegroundService(context, 111, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
        return pendingIntent as PendingIntent
    }

    private fun getErrorString(exception: Exception): String {
        return if (exception is ApiException) {
            return when (exception.statusCode) {
                GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> {
                    "GEOFENCE_NOT_AVAILABLE"
                }
                GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> {
                    "GEOFENCE_TOO_MANY_GEOFENCES"
                }
                GeofenceStatusCodes.GEOFENCE_INSUFFICIENT_LOCATION_PERMISSION -> {
                    "GEOFENCE_INSUFFICIENT_LOCATION_PERMISSION"
                }
                GeofenceStatusCodes.GEOFENCE_REQUEST_TOO_FREQUENT -> {
                    "GEOFENCE_REQUEST_TOO_FREQUENT"
                }
                GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> {
                    "GEOFENCE_TOO_MANY_PENDING_INTENTS"
                }
                else -> {
                    "invalid error code"
                }
            }
        } else {
            exception.localizedMessage ?: "invalid exception"
        }
    }

    fun addGeofence(latitude: Double, longitude: Double, geofenceRadius: Float) {
        val geofencingClient = LocationServices.getGeofencingClient(context)
        val geofenceHelper = GeofenceHelper.getInstance(context)
        val id = (System.currentTimeMillis() / 1000)
        val idString = id.toString()
        val geofence = geofenceHelper.getGeofence(
            idString, LatLng(latitude, longitude), geofenceRadius,
            Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT
        )
        val geofencingRequest = geofenceHelper.getGeofenceRequest(geofence)
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        geofencingClient.addGeofences(geofencingRequest, geofenceHelper.getPendingIntent())
            .addOnSuccessListener {

                GlobalScope.launch(Main) {
                    Toast.makeText(context, R.string.added_geofence, Toast.LENGTH_SHORT).show()
                    withContext(IO) {
                        DatabaseHelper.getInstance(context).addGeoInfo(GeoInfo(id, LatLng(latitude, longitude), getFormattedDateTime()))
                    }
                }
            }
            .addOnFailureListener {
                GlobalScope.launch(Main) {
                    Toast.makeText(context, getErrorString(it), Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun addGeofence(latitude: Double, longitude: Double, geofenceRadius: Float, success: () -> Unit, failure: (error: String) -> Unit) {
        val geofencingClient = LocationServices.getGeofencingClient(context)
        val geofenceHelper = GeofenceHelper.getInstance(context)
        val id = (System.currentTimeMillis() / 1000)
        val idString = id.toString()
        val geofence = geofenceHelper.getGeofence(
            idString, LatLng(latitude, longitude), geofenceRadius,
            Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT
        )
        val geofencingRequest = geofenceHelper.getGeofenceRequest(geofence)
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        geofencingClient.addGeofences(geofencingRequest, geofenceHelper.getPendingIntent()).run {
            this.addOnSuccessListener {
                Toast.makeText(context, R.string.added_geofence, Toast.LENGTH_SHORT).show()
                GlobalScope.launch(IO) {
                    DatabaseHelper.getInstance(context).addGeoInfo(GeoInfo(id, LatLng(latitude, longitude), getFormattedDateTime()))
                    success()
                }
            }
            this.addOnFailureListener {
                failure(getErrorString(it))
            }

        }
    }
}