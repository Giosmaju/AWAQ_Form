package com.example.backend_read.data.remote

object SessionManager {
    var tenant: String? = null
    var authToken: String? = null
    var apiKey: String? = null

    fun startSession(tenant: String, token: String, key: String) {
        this.tenant = tenant
        this.authToken = token
        this.apiKey = key
    }

    fun clear() {
        tenant = null
        authToken = null
        apiKey = null
    }
}
