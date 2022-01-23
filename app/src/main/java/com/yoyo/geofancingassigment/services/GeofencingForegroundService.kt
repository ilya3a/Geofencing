package com.yoyo.geofancingassigment.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.yoyo.geofancingassigment.R
import com.yoyo.geofancingassigment.geofence.GeofenceHelper
import com.yoyo.geofancingassigment.utils.GEOFENCE_RADIUS
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import android.os.PowerManager

import android.os.PowerManager.WakeLock
import androidx.annotation.RequiresApi


class GeofencingForegroundService : Service() {

    companion object {
        const val CHANNEL_ID = "ForegroundServiceChannel"
        const val CHANNEL_NAME = "Foreground Service Channel"
        const val LAT_KEY = "lat"
        const val LNG_KEY = "lng"
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
    val wakeLock: PowerManager.WakeLock =
        (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::MyWakelockTag").apply {
                acquire()
            }
        }

    Toast.makeText(baseContext, "onStartCommand", Toast.LENGTH_LONG).show()

    createNotificationChannel()
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(baseContext.getString(R.string.notification_title))
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .build()
        startForeground(1, notification)


        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            Toast.makeText(baseContext, "GEO FENCE ERROR", Toast.LENGTH_LONG).show()
            stopSelf()
            wakeLock.release()

        } else {

            val geofenceTransition = geofencingEvent.geofenceTransition

            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                val lat = geofencingEvent.triggeringLocation.latitude
                val lng = geofencingEvent.triggeringLocation.longitude


                GeofenceHelper.getInstance(baseContext).addGeofence(lat, lng, GEOFENCE_RADIUS, {
                    stopSelf()
                    wakeLock.release()

                }, {
                    Toast.makeText(baseContext, it, Toast.LENGTH_SHORT).show()
                    stopSelf()
                    wakeLock.release()

                })
            } else {
                // Log the error.
                Toast.makeText(baseContext, "GEO FENCE ERROR invalid type", Toast.LENGTH_LONG).show()
                stopSelf()
                wakeLock.release()
            }
        }


        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)
    }

}