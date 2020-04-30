package com.todomap.map

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.FirebaseFirestore

class MapViewModelFactory(
    private val firestore: FirebaseFirestore,
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            return modelClass.getConstructor(FirebaseFirestore::class.java, Application::class.java)
                .newInstance(firestore, application)
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
