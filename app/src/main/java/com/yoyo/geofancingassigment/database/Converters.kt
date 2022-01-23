package com.yoyo.geofancingassigment.database

import androidx.room.TypeConverter
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object Converters {
    @TypeConverter
    fun stringToLatLng(value: String): LatLng {
        val listType = object : TypeToken<LatLng>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun latLngToString(latLng: LatLng): String {
        return Gson().toJson(latLng)
    }
}