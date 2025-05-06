package com.dicoding.ticketingsystem.data.api

import com.dicoding.ticketingsystem.DataSource.Response.EventsResponse
import com.dicoding.ticketingsystem.DataSource.Response.UserTicketsResponse
import com.dicoding.ticketingsystem.data.request.LoginRequest
import com.dicoding.ticketingsystem.data.response.ApiResponse
import com.dicoding.ticketingsystem.data.response.LoginResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @POST("users/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<LoginResponse>

    @GET("tickets/user-tickets")
    suspend fun getUserTickets(
        @Query("user_id") userId: String? = null,
        @Query("status") status: String? = null,
        @Query("event_id") eventId: String? = null,
        @Query("group_by_event") groupByEvent: Boolean? = null
    ): ApiResponse<UserTicketsResponse>

    @GET("events/list")
    suspend fun getEvents(
        @Query("upcoming") upcoming: Boolean? = null,
        @Query("past") past: Boolean? = null,
        @Query("sort") sort: String? = "event_date",
        @Query("order") order: String? = "asc"
    ): EventsResponse
}