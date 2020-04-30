package com.todomap.database

data class Todo(
    var id: String = "",
    var title: String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var address: String = ""
)
