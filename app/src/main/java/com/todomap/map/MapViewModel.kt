package com.todomap.map

import android.app.Application
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.text.Editable
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.todomap.R
import com.todomap.database.Todo
import com.todomap.database.TodoDatabaseDao
import kotlinx.coroutines.*
import java.util.*

/**
 * @author WeiYi Yu
 * @date 2020-04-24
 */
class MapViewModel(
    private val databaseDao: TodoDatabaseDao,
    application: Application
) :
    AndroidViewModel(application) {

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

    private val _bottomSheetState = MutableLiveData<Int>().apply {
        // Initial state
        value = BottomSheetBehavior.STATE_HIDDEN
    }
    val bottomSheetState: LiveData<Int>
        get() = _bottomSheetState

    private var mapMarker: Marker? = null
    private val _markerLatLng = MutableLiveData<LatLng>()
    private val _markerAddress = MutableLiveData<String>()
    val markerAddress: LiveData<String>
        get() = _markerAddress

    val fabVisible = Transformations.map(_bottomSheetState) {
        it == BottomSheetBehavior.STATE_HIDDEN
    }

    private val _todoTitle = MutableLiveData<String>()

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

    fun onMarkerAdded(location: LatLng, marker: Marker) {
        uiScope.launch {
            mapMarker?.remove()
            mapMarker = marker

            _markerLatLng.value = location
            _markerAddress.value = getAddress(location)

            if (_bottomSheetState.value != BottomSheetBehavior.STATE_HIDDEN) {
                _bottomSheetState.value = BottomSheetBehavior.STATE_HALF_EXPANDED
            }
        }
    }

    fun createTodo(view: View) {
        uiScope.launch {
            val todo = Todo(
                title = _todoTitle.value!!,
                latitude = _markerLatLng.value!!.latitude,
                longitude = _markerLatLng.value!!.longitude
            )
            withContext(Dispatchers.IO) {
                databaseDao.insert(todo)

                for (aa in databaseDao.getAllTODOs()) {
                    println(aa.title)
                }
            }
            _snackbarEvent.value = "Todo created!"
            _bottomSheetState.value = BottomSheetBehavior.STATE_HIDDEN
        }
    }

    fun onTitleTextChanged(char: Editable) {
        _todoTitle.value = char.toString()
    }

    private suspend fun getAddress(location: LatLng) = withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(getApplication(), Locale.getDefault())
            val addresses: List<Address> =
                geocoder.getFromLocation(location.latitude, location.longitude, 1)
            addresses[0].getAddressLine(0)
        } catch (e: Exception) {
            e.message
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}