package com.yoyo.geofancingassigment.database.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng

@Entity(tableName = "geo_info")
data class GeoInfo(
    @PrimaryKey
    val timeStamp: Long,
    val latLng: LatLng,
    val date: String
)
