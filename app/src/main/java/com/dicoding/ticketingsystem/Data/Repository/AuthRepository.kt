package com.dicoding.ticketingsystem.data.repository

import com.dicoding.ticketingsystem.data.api.ApiConfig
import com.dicoding.ticketingsystem.data.request.LoginRequest
import com.dicoding.ticketingsystem.data.response.LoginResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository {
    private val apiService = ApiConfig.getApiService()

    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.login(LoginRequest(email, password))
                if (response.status == "success") {
                    Result.success(response.data)
                } else {
                    Result.failure(Exception(response.message))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}