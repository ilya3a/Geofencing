package com.yoyo.geofancingassigment.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.yoyo.geofancingassigment.BuildConfig
import com.yoyo.geofancingassigment.R
import com.yoyo.geofancingassigment.database.models.GeoInfo
import com.yoyo.geofancingassigment.databinding.ActivityMapsBinding
import com.yoyo.geofancingassigment.geofence.GeofenceHelper
import com.yoyo.geofancingassigment.utils.GEOFENCE_RADIUS
import kotlinx.coroutines.InternalCoroutinesApi

import android.util.Log
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.yoyo.geofancingassigment.database.DatabaseHelper
import com.yoyo.geofancingassigment.utils.TAG
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.withContext


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, LocationAdapter.OnItemClickListener {

    companion object {
        private const val FINE_LOCATION_REQ_CODE = 1001
        private const val BACKGROUND_LOCATION_REQ_CODE = 1002
    }

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var geofenceHelper: GeofenceHelper
    private lateinit var mapActivityViewModel: MapsViewModel
    private lateinit var mAdapter: LocationAdapter
    private var isRequestedLocationUpdates = false

    private val listOfGeoInfo = ArrayList<GeoInfo>()


    @InternalCoroutinesApi
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onResume() {
        super.onResume()
        if (!isRequestedLocationUpdates) {
            requestLocationUpdates()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            askForLocationPermission()
        }
        fusedLocationClient.requestLocationUpdates(getLocationRequest(), locationCallback, Looper.getMainLooper())
        isRequestedLocationUpdates = true
    }


    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        isRequestedLocationUpdates = false
    }

    @InternalCoroutinesApi
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivityMapsBinding.inflate(layoutInflater)
            mapActivityViewModel = ViewModelProvider(this).get(MapsViewModel::class.java)
            setContentView(binding.root)


            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                askForLocationPermission()
            }
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                if (checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    makeSnackbarBackgroundLocationPermission()
                }
            }
            normalOperation()
        } catch (t: Throwable) {
            Log.e(TAG, "onCreate: crush", t)
        }

    }

    private fun getLocationRequest(): LocationRequest {
        val request = LocationRequest.create()
        // get accuracy level
        request.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        // get update every...
        request.interval = 1000
        // the fastest update...
        request.fastestInterval = 500

        request.numUpdates = 1

        return request
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    @InternalCoroutinesApi
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        showUserLocation()
        setMapObservers()
        mapActivityViewModel.getGeoInfoList()
    }

    private fun setMapObservers() {
        mapActivityViewModel.geoInfoList.observe(this, { list ->
            mMap.clear()
            mAdapter.updateList(list)
            list.forEach { geoInfo ->
                addCircle(geoInfo.latLng.latitude, geoInfo.latLng.longitude, GEOFENCE_RADIUS)
            }
        })

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun showUserLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        mMap.isMyLocationEnabled = true

    }


    private fun addCircle(latitude: Double, longitude: Double, geofenceRadius: Float) {
        val circle = CircleOptions()
            .center(LatLng(latitude, longitude))
            .radius(geofenceRadius.toDouble())
            .strokeColor(Color.argb(200, 0, 255, 0))
            .strokeWidth(3f)
            .fillColor(Color.argb(64, 0, 255, 0))
        mMap.addCircle(circle)
    }

    private fun addMarker(latitude: Double, longitude: Double) {
        val marker = MarkerOptions()
            .position(LatLng(latitude, longitude))
        mMap.addMarker(marker)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun askForLocationPermission() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.")
            Snackbar.make(
                binding.root,
                R.string.permission_rationale,
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction(R.string.ok) {
                    // Request permission
                    ActivityCompat.requestPermissions(
                        this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                        FINE_LOCATION_REQ_CODE
                    )
                }
                .show()
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                FINE_LOCATION_REQ_CODE
            )
        }

    }

    @InternalCoroutinesApi
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            FINE_LOCATION_REQ_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //user gave permission
                    Toast.makeText(this, R.string.permission_granted, Toast.LENGTH_SHORT).show()
                    normalOperation()
                    showUserLocation()
                } else {
                    makeSnackbarBackgroundLocationPermission()
                }
            }
        }


    }

    private fun makeSnackbarBackgroundLocationPermission() {
        Snackbar.make(
            binding.root,
            R.string.permission_denied_explanation,
            Snackbar.LENGTH_INDEFINITE
        )
            .setAction(R.string.settings) { // Build intent that displays the App settings screen.
                val intent = Intent()
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                val uri: Uri = Uri.fromParts(
                    "package",
                    BuildConfig.APPLICATION_ID, null
                )
                intent.data = uri
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
            .show()
    }

    @InternalCoroutinesApi
    private fun normalOperation() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setRecyclerView()

        geofenceHelper = GeofenceHelper.getInstance(this)
        geofencingClient = LocationServices.getGeofencingClient(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            @RequiresApi(Build.VERSION_CODES.Q)
            override fun onLocationResult(location: LocationResult) {
                val location = location.lastLocation
                updateMapCamera(LatLng(location.latitude, location.longitude))
                lifecycleScope.launchWhenResumed {
                    withContext(IO) {
                        DatabaseHelper.getInstance(this@MapsActivity).getGeoInfoList().collect(object : FlowCollector<List<GeoInfo>> {
                            override suspend fun emit(value: List<GeoInfo>) {
                                if (value.isEmpty()) {
                                    withContext(Main) {
                                        if (checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                            geofenceHelper.addGeofence(location.latitude, location.longitude, GEOFENCE_RADIUS)

                                        }
                                    }
                                }
                            }
                        })
                    }
                }
            }
        }

    }

    private fun setRecyclerView() {
        mAdapter = LocationAdapter(arrayListOf(), this)
        binding.recycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recycler.adapter = mAdapter
    }

    private fun updateMapCamera(latLng: LatLng) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onItemClicked(lat: Double, lng: Double) {
        addMarker(lat, lng)
//        stopLocationUpdates()
        updateMapCamera(LatLng(lat, lng))
        Handler(Looper.getMainLooper()).postDelayed({
//            requestLocationUpdates()
        }, 5 * 1000)
    }

}