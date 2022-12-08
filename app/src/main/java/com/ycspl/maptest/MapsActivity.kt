package com.ycspl.maptest

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.ycspl.maptest.databinding.ActivityMapsBinding
import com.ycspl.maptest.utility.*
import com.ycspl.maptest.viewmodel.MapViewModel


class MapsActivity :  AppCompatActivity(), OnMapReadyCallback,
    LocationListener, GoogleMap.OnCameraMoveListener,
    GoogleMap.OnCameraIdleListener, GoogleMap.OnCameraMoveStartedListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    private lateinit var viewModel: MapViewModel
    private lateinit var userCurrentLocation: Location

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                getMyLocation()
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                getMyLocation()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getMyLocation() {
        createLocationSetting(this@MapsActivity)
        LocationServices.getFusedLocationProviderClient(this@MapsActivity)
            .lastLocation.addOnCompleteListener { task ->
                when (task.isSuccessful) {
                    true -> task.result?.let {
                        userCurrentLocation=it
                    }
                    else -> requestPermission()
                }
            }
    }

    private fun moveCamera(latLng: LatLng, zoom: Float = 15f) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        viewModel=ViewModelProvider(this).get(MapViewModel::class.java)
        setContentView(binding.root)

        binding.map.apply {
            onCreate(savedInstanceState)
            getMapAsync(this@MapsActivity)
        }

        requestPermission()

        initBottomSheet()

        handleFabClick()

        submitButtonClick()

        currentLocationClick()
    }

    private fun currentLocationClick() {
      binding.currentLocation.setOnClickListener{
          moveCamera(LatLng(userCurrentLocation.latitude, userCurrentLocation.longitude))
      }
    }

    private fun submitButtonClick() {
        binding.bottomSheetLayout.apply {
            btnSubmit.setOnClickListener {
                val propertyName = labelPropertyName.editText?.text
                if (propertyName.isNullOrEmpty()) {
                    showToast("Please Enter Property Name")
                    return@setOnClickListener
                }
                viewModel.insertLocation(propertyName.toString())
                showToast("Property Location Saved successfully")
                collapseBottomSheet(binding.fabButton)
            }
        }
    }

    private fun initBottomSheet() {
        BottomSheetBehavior.from(binding.sheet as View).apply {
            bottomSheetBehavior = this
            peekHeight = 0
            isDraggable = false
            this.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val isExpanded = bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED
        outState.putBoolean(STATE_EXPANDED, isExpanded)
        if (isExpanded) {
            val propertyName = binding.bottomSheetLayout.labelPropertyName.editText?.text ?: ""
            outState.putString(PROPERTY_NAME, propertyName.toString())
        }
        binding.map.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val isExpanded = savedInstanceState.getBoolean(STATE_EXPANDED)
        if (isExpanded) {
            val propertyName = savedInstanceState.getString(PROPERTY_NAME, "")
            expandBottomSheet(binding.fabButton, propertyName)
        }
    }

    private fun handleFabClick() {
        binding.fabButton.setOnClickListener {
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                collapseBottomSheet(it)
            } else {
                expandBottomSheet(it)
            }
        }
    }

    private fun collapseBottomSheet(view: View) {
        view.rotation = 0f
        mMap.clear()
        binding.mapOverlay.isClickable = false
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun expandBottomSheet(view: View, propertyName: String = "") {
        view.rotation = 45f
        binding.mapOverlay.isClickable = true
        binding.bottomSheetLayout.apply {
            labelPropertyName.editText?.setText(propertyName)
            labelPropertyCoordinate.editText?.setText(String.format(getString(R.string.coordinates), viewModel.latitude, viewModel.longitude))
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        if (::mMap.isInitialized)
            mMap.addMarker(MarkerOptions().position(LatLng(viewModel.latitude, viewModel.longitude)))
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.apply {
            mapType = GoogleMap.MAP_TYPE_SATELLITE
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED)
                mMap.addMarker(MarkerOptions().position(LatLng(viewModel.latitude, viewModel.longitude)))
            moveCamera(CameraUpdateFactory.zoomTo(15f))
            setOnCameraIdleListener(this@MapsActivity)
            setOnCameraMoveStartedListener(this@MapsActivity)
            setOnCameraMoveListener(this@MapsActivity)
        }
    }

    private fun requestPermission() {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    override fun onLocationChanged(location: Location) {
        viewModel.latitude = location.latitude
        viewModel.longitude = location.longitude
    }

    override fun onCameraMove() {
    }

    override fun onCameraIdle() {
        viewModel.latitude = mMap.cameraPosition.target.latitude
        viewModel.longitude = mMap.cameraPosition.target.longitude
    }

    override fun onCameraMoveStarted(p0: Int) {
    }

    override fun onResume() {
        binding.map.onResume()
        super.onResume()
    }

    override fun onPause() {
        binding.map.onPause()
        super.onPause()
    }

    override fun onLowMemory() {
        binding.map.onLowMemory()
        super.onLowMemory()
    }

    private fun showToast(message: String) {
        Toast
            .makeText(this@MapsActivity, message, Toast.LENGTH_SHORT)
            .show()
    }
}

