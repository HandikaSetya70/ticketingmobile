package com.dicoding.ticketingsystem.data.api

import com.dicoding.ticketingsystem.DataSource.Response.EventsResponse
import com.dicoding.ticketingsystem.DataSource.Response.PaymentVerificationResponse
import com.dicoding.ticketingsystem.DataSource.Response.PurchaseRequest
import com.dicoding.ticketingsystem.DataSource.Response.PurchaseResponse
import com.dicoding.ticketingsystem.DataSource.Response.UserProfileResponse
import com.dicoding.ticketingsystem.DataSource.Response.UserTicketsResponse
import com.dicoding.ticketingsystem.data.request.LoginRequest
import com.dicoding.ticketingsystem.data.response.ApiResponse
import com.dicoding.ticketingsystem.data.response.LoginResponse
import retrofit2.Response
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
        @Query("group_by_event") groupByEvent: Boolean? = null,
        @Query("include_qr") includeQr: Boolean? = true,
        @Query("upcoming_only") upcomingOnly: Boolean? = false
    ): ApiResponse<UserTicketsResponse>

    @GET("events/list")
    suspend fun getEvents(
        @Query("upcoming") upcoming: Boolean? = null,
        @Query("past") past: Boolean? = null,
        @Query("sort") sort: String? = "event_date",
        @Query("order") order: String? = "asc",
        @Query("category") category: String? = null,
        @Query("min_price") minPrice: Double? = null,
        @Query("max_price") maxPrice: Double? = null,
        @Query("available_only") availableOnly: Boolean? = true,
        @Query("page") page: Int? = 1,
        @Query("limit") limit: Int? = 10
    ): EventsResponse

    @GET("users/get")
    suspend fun getUserProfile(): UserProfileResponse

    // 🔧 FIXED: Remove Response<T> wrapper since your APIs return ApiResponse<T> directly
    @POST("tickets/buy")
    suspend fun purchaseTickets(@Body request: PurchaseRequest): PurchaseResponse

    // 🔧 FIXED: Remove Response<T> wrapper since your APIs return ApiResponse<T> directly
    @GET("payments/verify")
    suspend fun verifyPayment(
        @Query("payment_id") paymentId: String,
        @Query("paypal_order_id") paypalOrderId: String? = null
    ): PaymentVerificationResponse
}