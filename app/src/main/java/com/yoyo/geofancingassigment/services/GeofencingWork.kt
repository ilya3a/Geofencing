package com.yoyo.geofancingassigment.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.work.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.yoyo.geofancingassigment.R
import com.yoyo.geofancingassigment.database.models.GeoInfo
import com.yoyo.geofancingassigment.geofence.GeofenceHelper
import com.yoyo.geofancingassigment.utils.GEOFENCE_RADIUS
import java.util.concurrent.TimeUnit

class GeofencingWork(val context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    companion object {
        fun scheduleWork(context: Context, geoInfo: GeoInfo) {
            val data = Data.Builder().putString(GEOINFO_OBJECT, Gson().toJson(geoInfo)).build()

            val updateAppsWorkRequest = OneTimeWorkRequestBuilder<GeofencingWork>()
                .setInputData(data)
//                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setBackoffCriteria(BackoffPolicy.LINEAR, 20, TimeUnit.SECONDS)
                .addTag(GeofencingWork::class.java.simpleName)
                .build()

            WorkManager
                .getInstance(context)
                .enqueue(
//                    GeofencingWork::class.java.simpleName,
//                    ExistingWorkPolicy.REPLACE,
                    updateAppsWorkRequest
                )
        }

        private const val GEOINFO_OBJECT = "geoInfoObject"
        const val CHANNEL_ID = "ForegroundServiceChannel"
        const val CHANNEL_NAME = "Foreground Service Channel"
    }


    override suspend fun doWork(): Result {
        setForeground(createForegroundInfo())
        val type = object : TypeToken<GeoInfo>() {}.type
        val geoInfo = Gson().fromJson(inputData.getString(GEOINFO_OBJECT), type) as GeoInfo
        GeofenceHelper.getInstance(context).addGeofence(geoInfo.latLng.latitude, geoInfo.latLng.longitude, GEOFENCE_RADIUS)
//        DatabaseHelper.getInstance(context).addGeoInfo(geoInfo)
        return Result.success()
    }

    private fun createForegroundInfo(): ForegroundInfo {
        // Use a different id for each Notification.
        val notificationId = 1
        return ForegroundInfo(notificationId, createNotification())
    }

    /**
     * Create the notification and required channel (O+) for running work
     * in a foreground service.
     */
    private fun createNotification(): Notification {
        // This PendingIntent can be used to cancel the Worker.
        val intent = WorkManager.getInstance(context).createCancelPendingIntent(id)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(applicationContext.getString(R.string.notification_title))
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setOngoing(true)
            .addAction(R.drawable.ic_delete, "cancel", intent)
        createNotificationChannel(CHANNEL_ID, CHANNEL_NAME).also {
            builder.setChannelId(it.id)

        }
        return builder.build()
    }

    /**
     * Create the required notification channel for O+ devices.
     */
    private fun createNotificationChannel(
        channelId: String,
        name: String
    ): NotificationChannel {
        return NotificationChannel(
            channelId, name, NotificationManager.IMPORTANCE_LOW
        ).also { channel ->
            getSystemService(context, NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }
}