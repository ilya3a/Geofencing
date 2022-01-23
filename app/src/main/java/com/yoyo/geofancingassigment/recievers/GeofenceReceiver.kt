package com.yoyo.geofancingassigment.recievers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.google.android.gms.maps.model.LatLng
import com.yoyo.geofancingassigment.database.models.GeoInfo
import com.yoyo.geofancingassigment.services.GeofencingForegroundService
import com.yoyo.geofancingassigment.services.GeofencingForegroundService.Companion.LAT_KEY
import com.yoyo.geofancingassigment.services.GeofencingForegroundService.Companion.LNG_KEY
import com.yoyo.geofancingassigment.services.GeofencingWork
import com.yoyo.geofancingassigment.utils.getFormattedDateTime
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class GeofenceReceiver : BroadcastReceiver() {
    private fun createNotificationChannel(context: Context) {
        val serviceChannel = NotificationChannel(
            GeofencingForegroundService.CHANNEL_ID,
            GeofencingForegroundService.CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)
    }

    @DelicateCoroutinesApi
    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        GlobalScope.launch(Main) {
            Toast.makeText(context, "GeofenceReceiver", Toast.LENGTH_LONG).show()
        }
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            GlobalScope.launch(Main) {
                Toast.makeText(context, "GEO FENCE ERROR", Toast.LENGTH_LONG).show()
            }
            return
        }
        val geofenceTransition = geofencingEvent.geofenceTransition

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            //option 1 : foreground service
            Intent(context, GeofencingForegroundService::class.java).apply {
                putExtra(LAT_KEY, geofencingEvent.triggeringLocation.latitude)
                putExtra(LNG_KEY, geofencingEvent.triggeringLocation.longitude)
                ContextCompat.startForegroundService(context,this)
            }

            //option 2 : to do it straight on the receiver
//            GlobalScope.launch(Main) {
//                GeofenceHelper.getInstance(context).addGeofence(
//                    geofencingEvent.triggeringLocation.latitude,
//                    geofencingEvent.triggeringLocation.longitude,
//                    GEOFENCE_RADIUS
//                )
//
//            }


            //option 3 : job service

//            val extras = PersistableBundle()
//            extras.putDouble(LAT_KEY, geofencingEvent.triggeringLocation.latitude)
//            extras.putDouble(LNG_KEY, geofencingEvent.triggeringLocation.longitude)
//            GeofenceJobService.scheduleJob(context, extras)


            //option 4: work manager
//            val newGeofence = GeoInfo(
//                System.currentTimeMillis(),
//                LatLng(geofencingEvent.triggeringLocation.latitude, geofencingEvent.triggeringLocation.longitude),
//                getFormattedDateTime()
//            )
//
//            GeofencingWork.scheduleWork(context, newGeofence)
        } else {
            // Log the error.
            GlobalScope.launch(Main) {
                Toast.makeText(context, "GEO FENCE ERROR invalid type", Toast.LENGTH_LONG).show()
            }
        }
    }
}
