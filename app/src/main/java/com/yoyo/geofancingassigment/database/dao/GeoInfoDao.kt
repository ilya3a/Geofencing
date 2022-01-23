package com.yoyo.geofancingassigment.database.dao

import androidx.room.*
import com.yoyo.geofancingassigment.database.Converters
import com.yoyo.geofancingassigment.database.models.GeoInfo
import kotlinx.coroutines.flow.Flow

@Dao
@TypeConverters(Converters::class)
interface GeoInfoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addGeoInfoList(geoInfoList: List<GeoInfo>)

    @Query("DELETE FROM geo_info")
    fun deleteAllGeoInfo()

    @Query("SELECT * FROM geo_info ORDER BY timeStamp")
    fun getGeoInfoList(): Flow<List<GeoInfo>>

}
