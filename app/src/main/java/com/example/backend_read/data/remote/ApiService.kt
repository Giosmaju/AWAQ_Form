package com.example.backend_read.data.remote

import com.example.backend_read.data.model.ApiResponse
import com.example.backend_read.data.model.LoginRequest
import com.example.backend_read.data.model.LoginResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("api/{tenant}/users/login")
    suspend fun login(
        @Header("X-API-Key") apiKey: String,
        @Path("tenant") tenant: String,
        @Body request: LoginRequest
    ): LoginResponse

    @GET("api/{tenant}/forms")
    suspend fun getFilteredSubmissions(
        @Header("X-API-Key") apiKey: String,
        @Path("tenant") tenant: String,
        @Query("startDate") startDate: String?,
        @Query("endDate") endDate: String?,
        @Query("userId") userId: String?,
        @Query("cropType") cropType: String?,
        @Query("cropStatus") cropStatus: String?
    ): ApiResponse
}
