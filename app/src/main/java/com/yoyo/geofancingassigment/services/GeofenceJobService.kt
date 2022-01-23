package com.yoyo.geofancingassigment.services

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.app.job.JobScheduler

import android.app.job.JobInfo

import android.content.ComponentName
import android.os.PersistableBundle
import com.yoyo.geofancingassigment.geofence.GeofenceHelper
import com.yoyo.geofancingassigment.utils.GEOFENCE_RADIUS


class GeofenceJobService : JobService() {
    companion object{
        const val LAT_KEY = "lat"
        const val LNG_KEY = "lng"

        fun scheduleJob(context: Context, extras: PersistableBundle){
            val serviceComponent = ComponentName(context, GeofenceJobService::class.java)
            val builder = JobInfo.Builder(0, serviceComponent)
            builder.setMinimumLatency((150).toLong()) // wait at least
            builder.setOverrideDeadline((300).toLong()) // maximum delay
            builder.setExtras(extras)
            val jobScheduler = context.getSystemService(JobScheduler::class.java)
            jobScheduler.schedule(builder.build())
        }
    }

    override fun onStartJob(params: JobParameters): Boolean {
        val lat = params.extras.getDouble(LAT_KEY)
        val lng = params.extras.getDouble(LNG_KEY)

        GeofenceHelper.getInstance(applicationContext).addGeofence(lat,lng, GEOFENCE_RADIUS)
        return false
    }

    override fun onStopJob(params: JobParameters): Boolean {
        jobFinished(params,false)
        return false
    }
}