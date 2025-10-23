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

/**
 * Retrofit service interface for the backend API.
 */
interface ApiService {

    /**
     * Authenticates a user for a specific tenant.
     */
    @POST("api/{tenant}/users/login")
    suspend fun login(
        @Header("X-API-Key") apiKey: String,
        @Path("tenant") tenant: String,
        @Body request: LoginRequest
    ): LoginResponse

    /**
     * Retrieves a list of form submissions for the currently logged-in user's tenant.
     */
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
