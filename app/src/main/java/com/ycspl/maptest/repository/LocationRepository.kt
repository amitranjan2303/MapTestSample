package com.ycspl.maptest.repository

import androidx.lifecycle.LiveData
import com.ycspl.maptest.MapTestApplication
import com.ycspl.maptest.database.LocationEntity
import com.ycspl.maptest.database.MapTestAppDb

open class LocationRepository {

    private var appDb: MapTestAppDb? = MapTestAppDb.getDataBase(MapTestApplication.getAppContext()!!)

    fun getLocations(): LiveData<List<LocationEntity>>? = appDb?.getLocationDao()?.getAllLocations()

    suspend fun insert(location: LocationEntity) = appDb?.getLocationDao()?.insert(location)

    suspend fun deleteLocation(location: LocationEntity) = appDb?.getLocationDao()?.delete(location)
}