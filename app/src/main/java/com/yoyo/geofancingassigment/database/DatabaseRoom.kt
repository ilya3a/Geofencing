package com.yoyo.geofancingassigment.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.yoyo.geofancingassigment.database.dao.GeoInfoDao
import com.yoyo.geofancingassigment.database.models.GeoInfo


private const val DATABASE = "Database-Room"

@Database(entities = [GeoInfo::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class DatabaseRoom : RoomDatabase() {

    abstract fun geoInfoDao(): GeoInfoDao


    companion object {
        @Volatile
        private var iInstance: DatabaseRoom? = null
        fun getDatabase(context: Context): DatabaseRoom {
            val tempInstance = iInstance
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(context.applicationContext, DatabaseRoom::class.java, DATABASE).build()
                iInstance = instance
                return instance
            }
        }
    }
}