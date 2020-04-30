package com.todomap.map

import android.app.Application
import android.location.Address
import android.location.Geocoder
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.material.bottomnavigation.BottomNavigationView
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
) : AndroidViewModel(application), BottomNavigationView.OnNavigationItemSelectedListener {

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val locationProvider: LocationProvider = LocationProvider(application)

    //region GoogleMaps related
    private val _isMapReady = MutableLiveData<Boolean>()
    val isMapReady: LiveData<Boolean>
        get() = _isMapReady

    private val _location = MutableLiveData<LatLng>()
    val cameraPosition = Transformations.map(_location) {
        CameraUpdateFactory.newCameraPosition(
            CameraPosition.Builder()
                .target(LatLng(it.latitude, it.longitude))
                .zoom(17f)
                .build()
        )
    }
    private var mapMarker: Marker? = null
    val markerAddress = MutableLiveData<String>()
    //endregion

    //region bottomSheet
    private val _bottomSheetState = MutableLiveData<Int>().apply {
        // Initial state
        value = BottomSheetBehavior.STATE_HIDDEN
    }
    val bottomSheetState: LiveData<Int>
        get() = _bottomSheetState
    //endregion

    private val _snackbarEvent = MutableLiveData<String>()
    val snackbarEvent: LiveData<String>
        get() = _snackbarEvent

    private val _bottomNavigationSelectedItem = MutableLiveData<MenuItem>()
    val bottomNavigationSelectedItem: LiveData<MenuItem>
        get() = _bottomNavigationSelectedItem

    val todoTitle = MutableLiveData<String>()
    val allTodoList = databaseDao.getAllTODOs()

    val fabVisible = Transformations.map(_bottomSheetState) {
        it == BottomSheetBehavior.STATE_HIDDEN
    }

    val todoListVisible = Transformations.map(_bottomNavigationSelectedItem) {
        _bottomNavigationSelectedItem.value?.itemId == R.id.navigation_todo
    }

    fun onMapReady() {
        _isMapReady.value = true
    }

    fun requestLastLocation() {
        uiScope.launch {
            try {
                val location = locationProvider.getLastLocation().await()
                _location.value = LatLng(location.latitude, location.longitude)
            } catch (e: Exception) {
                _snackbarEvent.value = e.message
            }
        }
    }

    fun onPermissionDenied() {
        _snackbarEvent.value = getApplication<Application>().getString(R.string.location_required)
    }

    fun onFabClicked(view: View) {
        _bottomSheetState.value = BottomSheetBehavior.STATE_EXPANDED
    }

    fun onBottomSheetClosed() {
        _bottomSheetState.value = BottomSheetBehavior.STATE_HIDDEN
    }

    fun onMarkerAdded(location: LatLng, marker: Marker) {
        uiScope.launch {
            mapMarker?.remove()
            mapMarker = marker

            _location.value = location
            markerAddress.value = getAddress(location)

            if (_bottomSheetState.value != BottomSheetBehavior.STATE_HIDDEN) {
                _bottomSheetState.value = BottomSheetBehavior.STATE_HALF_EXPANDED
            }
        }
    }

    fun createTodo(view: View) {
        uiScope.launch {
            val todo = Todo(
                title = todoTitle.value!!,
                latitude = _location.value!!.latitude,
                longitude = _location.value!!.longitude
            )
            withContext(Dispatchers.IO) {
                databaseDao.insert(todo)
            }

            markerAddress.value = ""
            todoTitle.value = ""
            _snackbarEvent.value = "Todo created!"
            _bottomSheetState.value = BottomSheetBehavior.STATE_HIDDEN
        }
    }

    fun onTodoClicked(todoId: Long) {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                databaseDao.delete(todoId)
            }
        }
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

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        _bottomNavigationSelectedItem.value = item
        return false
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}