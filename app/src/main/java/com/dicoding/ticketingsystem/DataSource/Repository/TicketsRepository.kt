package com.dicoding.ticketingsystem.DataSource.Repository

import android.content.Context
import android.util.Log
import com.dicoding.ticketingsystem.DataSource.Response.UserTicketsResponse
import com.dicoding.ticketingsystem.data.SessionManager
import com.dicoding.ticketingsystem.data.api.ApiConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class TicketsRepository(context: Context) {
    private val apiService = ApiConfig.getAuthenticatedApiService(context)
    private val TAG = "TicketsRepository"

    suspend fun getUserTickets(
        userId: String? = null,
        status: String? = null,
        eventId: String? = null,
        groupByEvent: Boolean = false
    ): Result<UserTicketsResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Making API call to get tickets")
                val response = apiService.getUserTickets(
                    userId = userId,
                    status = status,
                    eventId = eventId,
                    groupByEvent = if (groupByEvent) true else null
                )

                Log.d(TAG, "Received response status: ${response.status}, message: ${response.message}")

                if (response.status == "success") {
                    Log.d(TAG, "Success: Retrieved ${response.data.total_tickets} tickets")
                    Result.success(response.data)
                } else {
                    Log.e(TAG, "API error: ${response.message}")
                    Result.failure(Exception(response.message))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception getting tickets", e)
                Result.failure(e)
            }
        }
    }
}