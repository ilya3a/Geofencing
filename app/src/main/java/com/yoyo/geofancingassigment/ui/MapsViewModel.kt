package com.yoyo.geofancingassigment.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.yoyo.geofancingassigment.database.DatabaseHelper
import com.yoyo.geofancingassigment.database.models.GeoInfo
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch

class MapsViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application
    private val databaseHelper = DatabaseHelper.getInstance(context)


    val geoInfoList = MutableLiveData<ArrayList<GeoInfo>>()


    @InternalCoroutinesApi
    fun getGeoInfoList() {
        viewModelScope.launch(IO) {
            databaseHelper.getGeoInfoList().collect(object : FlowCollector<List<GeoInfo>> {
                override suspend fun emit(value: List<GeoInfo>) {
                    geoInfoList.postValue(value as ArrayList<GeoInfo>)
                }
            })
        }
    }
}