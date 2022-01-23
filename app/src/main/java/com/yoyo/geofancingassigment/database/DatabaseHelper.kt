package com.yoyo.geofancingassigment.database

import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.lifecycle.LiveData
import com.yoyo.geofancingassigment.database.models.GeoInfo
import com.yoyo.geofancingassigment.utils.SingletonHolder
import kotlinx.coroutines.flow.Flow

class DatabaseHelper private constructor(context: Context) {
    companion object : SingletonHolder<DatabaseHelper, Context>(::DatabaseHelper) {
        private const val APP_PREFS = "app_prefs"
        private const val FIRST_LAUNCH = "first_launch"
    }

    private val sharedPref = context.getSharedPreferences(APP_PREFS, MODE_PRIVATE)
    private val geoInfoDao = DatabaseRoom.getDatabase(context).geoInfoDao()

    fun isFirstLaunch(): Boolean {
        return sharedPref.getBoolean(FIRST_LAUNCH, true)
    }

    fun setFirstLaunch() {
        sharedPref.edit().putBoolean(FIRST_LAUNCH, false).commit()
    }

    fun addGeoInfoList(geoInfoList: List<GeoInfo>) {
        geoInfoDao.addGeoInfoList(geoInfoList)
    }

    fun addGeoInfo(geoInfo: GeoInfo) {
        geoInfoDao.addGeoInfoList(listOf(geoInfo))
    }

    fun getGeoInfoList(): Flow<List<GeoInfo>> {
        return geoInfoDao.getGeoInfoList()
    }

}