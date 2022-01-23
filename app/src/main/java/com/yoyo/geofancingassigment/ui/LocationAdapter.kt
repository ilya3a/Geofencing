package com.yoyo.geofancingassigment.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import com.yoyo.geofancingassigment.R
import com.yoyo.geofancingassigment.database.models.GeoInfo

class LocationAdapter(var mList: ArrayList<GeoInfo>, private val mListener: OnItemClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    interface OnItemClickListener {
        fun onItemClicked(lat:Double,lng:Double)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return GeoInfoViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_geo_info, parent, false),
            mListener
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is GeoInfoViewHolder -> {
                holder.onBind(mList[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    fun updateList(list: ArrayList<GeoInfo>) {
        mList = list
        notifyItemRangeChanged(0, mList.size)
    }
}
