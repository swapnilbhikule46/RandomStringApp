package com.example.randomstringapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Entity(tableName = "random_string_data")
data class RandomStringData(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val value: String,
    val length: Int,
    val created: String
)
{
    /**
     * Returns a formatted date string for UI display
     */
    fun getFormattedDate(): String {
        val instant = Instant.parse(created)
        val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm:ss")
            .withLocale(Locale.getDefault())
            .withZone(ZoneId.systemDefault())
        return formatter.format(instant)
    }
}

