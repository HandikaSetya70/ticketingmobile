package com.dicoding.ticketingsystem.DataSource.Network.Interceptor

import com.dicoding.ticketingsystem.data.SessionManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val sessionManager: SessionManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Create a new request with auth header
        val requestBuilder = originalRequest.newBuilder()
            .header("Content-Type", "application/json")

        // Get the token synchronously (this is safe in an interceptor)
        runBlocking {
            val token = sessionManager.accessToken.first()
            if (!token.isNullOrEmpty()) {
                requestBuilder.header("Authorization", "Bearer $token")
            }
        }

        val request = requestBuilder.build()
        return chain.proceed(request)
    }
}