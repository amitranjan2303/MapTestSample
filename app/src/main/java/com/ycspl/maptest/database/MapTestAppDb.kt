package com.ycspl.maptest.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ycspl.maptest.DATABASE_VERSION


@Database(entities = [LocationEntity::class], version = DATABASE_VERSION, exportSchema = false)
abstract class MapTestAppDb : RoomDatabase() {

    companion object {
        private var INSTANCE: MapTestAppDb? = null
        fun getDataBase(context: Context): MapTestAppDb {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(
                    context.applicationContext,
                    MapTestAppDb::class.java,
                    "meal-app-db"
                ).allowMainThreadQueries().build()
            }
            return INSTANCE as MapTestAppDb
        }
    }

    abstract fun getLocationDao(): LocationDao
}