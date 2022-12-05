package com.ycspl.maptest

import android.app.Activity
import android.content.IntentSender.SendIntentException
import android.util.Log
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest.Builder
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.tasks.Task

fun getLocationRequest() = LocationRequest
    .Builder(UPDATE_INTERVAL_IN_MILLISECONDS)
    .setPriority(100)
    .build()

fun createLocationSetting(activity: Activity) {

    val builder = Builder()
        .addLocationRequest(getLocationRequest())

    val result: Task<LocationSettingsResponse> = LocationServices
        .getSettingsClient(activity)
        .checkLocationSettings(builder.build())
    result.addOnSuccessListener {
        Log.d("onSuccess", "Start Location updates")
    }

    result.addOnCanceledListener {
        Log.d("onCancel", "cancel Location updates")
    }
    result.addOnFailureListener { exception ->
        when ((exception as ApiException).statusCode) {
            LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                var newException = exception as? ResolvableApiException
                if (newException == null) {
                    newException = ResolvableApiException(exception.status)
                }

                try {
                    newException.startResolutionForResult(
                        activity,
                        REQUEST_CHECK_SETTINGS
                    )
                } catch (e: SendIntentException) {
                    // Ignore the error.
                }
            }

            LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                // Location settings are not satisfied. However, we have no way to fix the
                // settings so we won't show the dialog.
            }
        }
    }
}