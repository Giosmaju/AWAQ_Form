package com.example.backend_read.data.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("user_email") val email: String,
    @SerializedName("password") val password: String
)

data class LoginResponse(
    @SerializedName("message") val message: String,
    @SerializedName("user") val user: User,
    @SerializedName("token") val token: String
)

data class User(
    @SerializedName("id") val id: Int,
    @SerializedName("username") val username: String,
    @SerializedName("user_email") val email: String,
    @SerializedName("tenant") val tenant: String
)

data class ApiResponse(
    @SerializedName("data") val data: List<Submission>
)

data class Submission(
    @SerializedName("id") val id: Long,
    @SerializedName("id_usuario") val userId: Int?,
    @SerializedName("cultivo") val cultivo: String,
    @SerializedName("fecha_siembra") val fechaSiembra: String,
    @SerializedName("humedad") val humedad: Int?,
    @SerializedName("metodo_humedad") val metodoHumedad: String?,
    @SerializedName("ph") val ph: Int?,
    @SerializedName("metodo_ph") val metodoPh: String?,
    @SerializedName("altura_planta") val alturaPlanta: Double?,
    @SerializedName("metodo_altura") val metodoAltura: String?,
    @SerializedName("estado_fenologico") val estadoFenologico: String?,
    @SerializedName("densidad_follaje") val densidadFollaje: String?,
    @SerializedName("color_follaje") val colorFollaje: String?,
    @SerializedName("estado_follaje") val estadoFollaje: String?,
    @SerializedName("observaciones") val observaciones: String?,
    @SerializedName("estado") val estado: Int?,
    @SerializedName("localizacion") val localizacion: String?,
    @SerializedName("image_url") val imageUrl: String?
)

data class FilterState(
    val selectedUserIds: List<String> = emptyList(),
    val startDate: String? = null,
    val endDate: String? = null,
    val selectedCropTypes: List<String> = emptyList(),
    val selectedCropStatus: List<String> = emptyList()
)

sealed class UiState {
    data object Loading : UiState()
    data class Success(val submissions: List<Submission>) : UiState()
    data class Error(val message: String) : UiState()
    data object Empty : UiState()
}
