package com.yoyo.geofancingassigment.ui

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.yoyo.geofancingassigment.database.models.GeoInfo
import com.yoyo.geofancingassigment.databinding.ItemGeoInfoBinding

class GeoInfoViewHolder(itemView: View, private val listener: LocationAdapter.OnItemClickListener) : RecyclerView.ViewHolder(itemView) {
    private val mBinding = ItemGeoInfoBinding.bind(itemView)
    fun onBind(geoInfo: GeoInfo) {
        mBinding.date.text = geoInfo.date
        mBinding.itemLayout.setOnClickListener {
            listener.onItemClicked(geoInfo.latLng.latitude,geoInfo.latLng.longitude)
        }
    }
}
