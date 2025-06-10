package com.dicoding.ticketingsystem.DataSource.Repository

import android.content.Context
import android.util.Log
import com.dicoding.ticketingsystem.DataSource.Response.Event
import com.dicoding.ticketingsystem.DataSource.Response.EventsData
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
        order: String = "asc",
        // 🆕 NEW PARAMETERS for enhanced filtering
        category: String? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        availableOnly: Boolean? = true,
        page: Int? = 1,
        limit: Int? = 10
    ): Result<List<Event>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Fetching events with params - upcoming: $upcoming, past: $past, sort: $sort, order: $order")
                Log.d(TAG, "Enhanced params - category: $category, minPrice: $minPrice, maxPrice: $maxPrice, availableOnly: $availableOnly")

                val response = apiService.getEvents(
                    upcoming = upcoming,
                    past = past,
                    sort = sort,
                    order = order,
                    // 🆕 NEW PARAMETERS
                    category = category,
                    minPrice = minPrice,
                    maxPrice = maxPrice,
                    availableOnly = availableOnly,
                    page = page,
                    limit = limit
                )

                if (response.status == "success") {
                    // 🔄 UPDATED: Access events from response.data.events instead of response.data
                    val events = response.data.events
                    Log.d(TAG, "Successfully fetched ${events.size} events")

                    // 🆕 LOG ADDITIONAL INFO from enhanced API
                    response.data.pagination?.let { pagination ->
                        Log.d(TAG, "Pagination: page ${pagination.page}/${pagination.totalPages}, total: ${pagination.total}")
                    }

                    // 🆕 LOG PRICING INFO for first few events
                    events.take(3).forEach { event ->
                        Log.d(TAG, "Event: ${event.name} - Price: $${event.price}, Available: ${event.available}/${event.total}")
                    }

                    Result.success(events)
                } else {
                    Log.e(TAG, "API error: ${response.message}")
                    Result.failure(Exception(response.message))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching events", e)
                // 🆕 ENHANCED ERROR LOGGING
                when (e) {
                    is java.net.UnknownHostException -> {
                        Log.e(TAG, "Network error: No internet connection")
                        Result.failure(Exception("No internet connection. Please check your network."))
                    }
                    is java.net.SocketTimeoutException -> {
                        Log.e(TAG, "Network error: Request timeout")
                        Result.failure(Exception("Request timeout. Please try again."))
                    }
                    is retrofit2.HttpException -> {
                        Log.e(TAG, "HTTP error: ${e.code()} - ${e.message()}")
                        Result.failure(Exception("Server error (${e.code()}). Please try again later."))
                    }
                    else -> {
                        Result.failure(e)
                    }
                }
            }
        }
    }

    // 🆕 NEW METHOD: Get events with full EventsData response (for pagination, etc.)
    suspend fun getEventsWithData(
        upcoming: Boolean? = null,
        past: Boolean? = null,
        sort: String = "event_date",
        order: String = "asc",
        category: String? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        availableOnly: Boolean? = true,
        page: Int? = 1,
        limit: Int? = 10
    ): Result<EventsData> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Fetching events data with enhanced params")

                val response = apiService.getEvents(
                    upcoming = upcoming,
                    past = past,
                    sort = sort,
                    order = order,
                    category = category,
                    minPrice = minPrice,
                    maxPrice = maxPrice,
                    availableOnly = availableOnly,
                    page = page,
                    limit = limit
                )

                if (response.status == "success") {
                    Log.d(TAG, "Successfully fetched events data")
                    Result.success(response.data)
                } else {
                    Log.e(TAG, "API error: ${response.message}")
                    Result.failure(Exception(response.message))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching events data", e)
                Result.failure(e)
            }
        }
    }

    // 🆕 NEW METHOD: Get events by category
    suspend fun getEventsByCategory(category: String): Result<List<Event>> {
        return getEvents(
            upcoming = true,
            category = category,
            availableOnly = true
        )
    }

    // 🆕 NEW METHOD: Get events by price range
    suspend fun getEventsByPriceRange(minPrice: Double, maxPrice: Double): Result<List<Event>> {
        return getEvents(
            upcoming = true,
            minPrice = minPrice,
            maxPrice = maxPrice,
            availableOnly = true
        )
    }

    // 🆕 NEW METHOD: Search available events only
    suspend fun getAvailableEvents(): Result<List<Event>> {
        return getEvents(
            upcoming = true,
            availableOnly = true
        )
    }

    // 🆕 NEW METHOD: Get sold out events
    suspend fun getSoldOutEvents(): Result<List<Event>> {
        return getEvents(
            upcoming = true,
            availableOnly = false
        ).map { events ->
            events.filter { it.is_sold_out }
        }
    }
}