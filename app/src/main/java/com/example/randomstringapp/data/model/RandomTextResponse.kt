package com.example.randomstringapp.data.model
import com.google.gson.annotations.SerializedName

/**
 * Data classes for parsing the JSON response from the content provider
 */
data class RandomTextResponse(
    @SerializedName("randomText") val randomText: RandomTextData
)


