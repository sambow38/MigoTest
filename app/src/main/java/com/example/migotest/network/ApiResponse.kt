package com.example.migotest.network

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import java.util.*

class ApiResponse {
    @SerializedName("status")
    var status: Number = -1
    @SerializedName("message")
    var message: String? = null
}