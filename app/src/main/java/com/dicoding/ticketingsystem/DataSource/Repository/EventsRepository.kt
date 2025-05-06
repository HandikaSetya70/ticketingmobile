package com.dicoding.ticketingsystem.DataSource.Repository

import android.content.Context
import android.util.Log
import com.dicoding.ticketingsystem.DataSource.Response.Event
import com.dicoding.ticketingsystem.data.api.ApiConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EventsRepository(private val context: Context) {
    private val apiService = ApiConfig.getApiService() // Using unauthenticated service since this is public
    private val TAG = "EventsRepository"

    suspend fun getEvents(
        upcoming: Boolean? = null,
        past: Boolean? = null,
        sort: String = "event_date",
        order: String = "asc"
    ): Result<List<Event>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Fetching events with params - upcoming: $upcoming, past: $past, sort: $sort, order: $order")

                val response = apiService.getEvents(
                    upcoming = upcoming,
                    past = past,
                    sort = sort,
                    order = order
                )

                if (response.status == "success") {
                    Log.d(TAG, "Successfully fetched ${response.data.size} events")
                    Result.success(response.data)
                } else {
                    Log.e(TAG, "API error: ${response.message}")
                    Result.failure(Exception(response.message))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching events", e)
                Result.failure(e)
            }
        }
    }
}