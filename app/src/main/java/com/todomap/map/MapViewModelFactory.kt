package com.todomap.map

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MapViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            return modelClass.getConstructor(Application::class.java)
                .newInstance(application)
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
