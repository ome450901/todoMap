package com.todomap.map

import android.app.Application
import android.location.Address
import android.location.Geocoder
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.todomap.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*

/**
 * @author WeiYi Yu
 * @date 2020-04-24
 */
class MapViewModel(application: Application) : AndroidViewModel(application) {

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val locationProvider: LocationProvider = LocationProvider(application)

    private val _isMapReady = MutableLiveData<Boolean>()
    val isMapReady: LiveData<Boolean>
        get() = _isMapReady

    private val _location = MutableLiveData<Location>()
    val location: LiveData<Location>
        get() = _location

    private val _snackbarEvent = MutableLiveData<String>()
    val snackbarEvent: LiveData<String>
        get() = _snackbarEvent

    private val _bottomSheetState = MutableLiveData<Int>()
    val bottomSheetState: LiveData<Int>
        get() = _bottomSheetState

    private val _currentLocation = MutableLiveData<LatLng>()
    val currentLocation = Transformations.map(_currentLocation) {
        val geocoder = Geocoder(application, Locale.getDefault())
        val addresses: List<Address> = geocoder.getFromLocation(it.latitude, it.longitude, 1)
        addresses[0].getAddressLine(0)
    }

    val fabVisible = Transformations.map(_bottomSheetState) {
        it == BottomSheetBehavior.STATE_HIDDEN
    }

    init {
        _bottomSheetState.value = BottomSheetBehavior.STATE_HIDDEN
    }

    fun onMapReady() {
        _isMapReady.value = true
    }

    fun onRequestLastLocation() {
        uiScope.launch {
            try {
                val location = locationProvider.getLastLocation().await()
                _location.value = location
            } catch (e: Exception) {
                _snackbarEvent.value = e.message
            }
        }
    }

    fun onPermissionDenied() {
        _snackbarEvent.value = getApplication<Application>().getString(R.string.location_required)
    }

    fun onFabClicked() {
        _bottomSheetState.value = BottomSheetBehavior.STATE_EXPANDED
    }

    fun onBottomSheetClosed() {
        _bottomSheetState.value = BottomSheetBehavior.STATE_HIDDEN
    }

    fun onLocationUpdated(location: LatLng) {
        _currentLocation.value = location

        if (_bottomSheetState.value != BottomSheetBehavior.STATE_HIDDEN) {
            _bottomSheetState.value = BottomSheetBehavior.STATE_HALF_EXPANDED
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}