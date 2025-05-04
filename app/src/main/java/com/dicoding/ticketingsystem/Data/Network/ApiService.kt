package com.dicoding.ticketingsystem.data.api

import com.dicoding.ticketingsystem.data.request.LoginRequest
import com.dicoding.ticketingsystem.data.response.ApiResponse
import com.dicoding.ticketingsystem.data.response.LoginResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("users/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<LoginResponse>
}