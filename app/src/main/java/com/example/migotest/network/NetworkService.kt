package com.example.migotest.network

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface NetworkService {
    @GET("/status")
    fun restfulApi(): Call<ApiResponse>
}