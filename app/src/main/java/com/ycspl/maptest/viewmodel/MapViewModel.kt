package com.ycspl.maptest.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ycspl.maptest.database.LocationEntity
import com.ycspl.maptest.repository.LocationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MapViewModel : ViewModel() {

    private var repository: LocationRepository? = LocationRepository()

    fun insertLocation(propertyName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val model = LocationEntity(
                propertyName = propertyName,
                latitude = latitude,
                longitude = longitude
            )
            repository?.insert(model)
        }
    }

    /* For testing purpose only */
    fun getSavedLocation() {
        viewModelScope.launch {
            repository?.getLocations()?.value?.let { value ->
                Log.d("SavedLocation", "$value")
            }
        }
    }

    /* observe list to show in Recycler view */
    val getSavedLocation: LiveData<List<LocationEntity>>? = repository?.getLocations()

    var latitude: Double = 0.0
    var longitude: Double = 0.0

}