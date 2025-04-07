package com.example.randomstringapp.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.randomstringapp.data.model.RandomStringData


/**
 * Room database for storing generated random strings
 */
@Database(entities = [RandomStringData::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun randomStringDao(): RandomStringDao
}