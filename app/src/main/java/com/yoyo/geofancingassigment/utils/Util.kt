package com.yoyo.geofancingassigment.utils

import java.text.SimpleDateFormat
import java.util.*


const val GEOFENCE_RADIUS = 300f


fun getFormattedDateTime(): String {
    return SimpleDateFormat("dd-MM-yyyy_HH:mm:ss", Locale.getDefault()).format(Date())
}