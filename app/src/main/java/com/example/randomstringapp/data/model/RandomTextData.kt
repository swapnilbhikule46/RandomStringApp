package com.example.randomstringapp.data.model

import com.google.gson.annotations.SerializedName

data class RandomTextData(
    @SerializedName("value") val value: String,
    @SerializedName("length") val length: Int,
    @SerializedName("created") val created: String
)
