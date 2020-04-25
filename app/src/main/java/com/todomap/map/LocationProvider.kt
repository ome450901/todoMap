package com.todomap.map

import android.content.Context
import android.location.Location
import android.location.LocationManager
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.todomap.R
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred


/**
 * @author WeiYi Yu
 * @date 2020-04-24
 */

class LocationProvider(private val context: Context) {

    private val fusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager


    private val locationRequest: LocationRequest = LocationRequest.create()
        .apply {
            interval = INTERVAL
            fastestInterval = FASTEST_INTERVAL
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

    fun getLastLocation(): Deferred<Location> {
        val deferred = CompletableDeferred<Location>()

        if (isLocationEnabled().not()) {
            deferred.completeExceptionally(Exception(context.getString(R.string.turn_gps)))
        } else {
            fusedLocationProviderClient.lastLocation
                .addOnSuccessListener {
                    it?.let {
                        deferred.complete(it)
                    } ?: deferred.completeExceptionally(
                        Exception(context.getString(R.string.no_locaion_found))
                    )
                }
                .addOnFailureListener {
                    deferred.completeExceptionally(it)
                }
        }

        return deferred
    }

//    override fun onInactive() {
//        super.onInactive()
//        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
//    }

    private fun isLocationEnabled(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

//    private fun startLocationUpdates() {
//        fusedLocationProviderClient.requestLocationUpdates(
//            locationRequest,
//            locationCallback,
//            Looper.getMainLooper()
//        )
//    }

//    private val locationCallback = object : LocationCallback() {
//        override fun onLocationResult(locationResult: LocationResult?) {
//            if (locationResult == null) {
//                value = LocationResult(false, errorMessage = "No location found.")
//                return
//            }
//
//            for (location in locationResult.locations) {
//                value = LocationResult(true, location)
//            }
//        }
//    }

    companion object {
        private const val INTERVAL = 1000L
        private const val FASTEST_INTERVAL = 500L
    }
}