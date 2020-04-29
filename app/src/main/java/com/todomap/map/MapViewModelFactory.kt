package com.todomap.map

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.todomap.database.TodoDatabaseDao

class MapViewModelFactory(
    private val databaseDao: TodoDatabaseDao,
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            return modelClass.getConstructor(TodoDatabaseDao::class.java, Application::class.java)
                .newInstance(databaseDao, application)
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
