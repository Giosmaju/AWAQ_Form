package com.example.backend_read.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.backend_read.data.model.LoginRequest
import com.example.backend_read.data.remote.ApiService
import com.example.backend_read.data.remote.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Represents the state of the Login UI.
 */
sealed class LoginState {
    data object Idle : LoginState()
    data object Loading : LoginState()
    data object Success : LoginState()
    data class Error(val message: String) : LoginState()
}

/**
 * Manages the state and business logic for the Login screen.
 */
class LoginViewModel(private val apiService: ApiService) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    fun login(tenant: String, email: String, password: String, apiKey: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading

            if (tenant.isBlank() || email.isBlank() || password.isBlank() || apiKey.isBlank()) {
                _loginState.value = LoginState.Error("All fields are required.")
                return@launch
            }

            try {
                val request = LoginRequest(email = email, password = password)
                val response = apiService.login(apiKey, tenant, request)

                // Start a session with all required credentials
                SessionManager.startSession(
                    tenant = response.user.tenant,
                    token = response.token,
                    key = apiKey
                )

                _loginState.value = LoginState.Success
            } catch (e: Exception) {
                _loginState.value = LoginState.Error("Login failed: ${e.message}")
            }
        }
    }
}
