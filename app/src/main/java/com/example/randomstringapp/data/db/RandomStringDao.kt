package com.example.randomstringapp.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.randomstringapp.data.model.RandomStringData
import kotlinx.coroutines.flow.Flow

@Dao
interface RandomStringDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStrings(strings: RandomStringData): Long

    @Query("SELECT * FROM random_string_data")
    fun getAllStrings(): Flow<List<RandomStringData>>

    @Query("DELETE FROM random_string_data")
    suspend fun deleteAllStrings(): Int

    @Delete
    suspend fun deleteString(randomStringData: RandomStringData): Int
}
