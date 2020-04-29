package com.todomap.database

import androidx.lifecycle.LiveData
import androidx.room.*

/**
 * Defines methods for using the SleepNight class with Room.
 */
@Dao
interface TodoDatabaseDao {

    @Insert
    fun insert(todo: Todo)

    @Update
    fun update(todo: Todo)

    @Delete
    fun delete(todo: Todo)

    @Query("SELECT * from todo_table WHERE todoId = :id")
    fun get(id: Long): Todo?

    @Query("DELETE FROM todo_table")
    fun clear()

    @Query("SELECT * FROM todo_table ORDER BY todoId DESC")
    fun getAllTODOs(): LiveData<List<Todo>>
}

