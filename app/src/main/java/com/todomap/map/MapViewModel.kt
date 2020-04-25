package com.todomap.map

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.todomap.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

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


    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}