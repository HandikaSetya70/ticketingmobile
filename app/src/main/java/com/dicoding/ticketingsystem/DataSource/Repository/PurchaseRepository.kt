package com.dicoding.ticketingsystem.DataSource.Repository

import android.content.Context
import android.util.Log
import com.dicoding.ticketingsystem.DataSource.Response.DeviceInfo
import com.dicoding.ticketingsystem.DataSource.Response.PaymentVerificationData
import com.dicoding.ticketingsystem.DataSource.Response.PaymentVerificationResponse
import com.dicoding.ticketingsystem.DataSource.Response.PurchaseData
import com.dicoding.ticketingsystem.DataSource.Response.PurchaseRequest
import com.dicoding.ticketingsystem.DataSource.Response.PurchaseResponse
import com.dicoding.ticketingsystem.data.api.ApiConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PurchaseRepository(context: Context) {
    private val apiService = ApiConfig.getAuthenticatedApiService(context)
    private val TAG = "PurchaseRepository"

    /**
     * Initiate ticket purchase
     * Calls POST /tickets/buy endpoint
     */
    suspend fun purchaseTickets(request: PurchaseRequest): Result<PurchaseData> {
        return withContext(Dispatchers.IO) {
            try {
                // Enhanced logging to include bound names
                Log.d(TAG, "Making API call to purchase tickets:")
                Log.d(TAG, "  Event ID: ${request.event_id}")
                Log.d(TAG, "  Quantity: ${request.quantity}")
                Log.d(TAG, "  Bound Names: ${request.bound_names}")
                Log.d(TAG, "  Bound Names Count: ${request.bound_names.size}")
                Log.d(TAG, "  Device Info: ${request.device_info}")

                // Validate bound names before making API call
                if (request.bound_names.size != request.quantity) {
                    val errorMessage = "Bound names count (${request.bound_names.size}) must match quantity (${request.quantity})"
                    Log.e(TAG, errorMessage)
                    return@withContext Result.failure(Exception(errorMessage))
                }

                // Validate each bound name
                request.bound_names.forEachIndexed { index, name ->
                    if (name.isBlank()) {
                        val errorMessage = "Bound name for ticket ${index + 1} cannot be empty"
                        Log.e(TAG, errorMessage)
                        return@withContext Result.failure(Exception(errorMessage))
                    }
                    if (name.length > 50) {
                        val errorMessage = "Bound name for ticket ${index + 1} is too long (max 50 characters)"
                        Log.e(TAG, errorMessage)
                        return@withContext Result.failure(Exception(errorMessage))
                    }
                }

                // Check for duplicate names
                val uniqueNames = request.bound_names.distinct()
                if (uniqueNames.size != request.bound_names.size) {
                    val errorMessage = "Duplicate bound names are not allowed"
                    Log.e(TAG, errorMessage)
                    return@withContext Result.failure(Exception(errorMessage))
                }

                Log.d(TAG, "✅ Bound names validation passed")

                val response = apiService.purchaseTickets(request)

                Log.d(TAG, "Received purchase response:")
                Log.d(TAG, "  Status: ${response.status}")
                Log.d(TAG, "  Message: ${response.message}")

                if (response.status == "success") {
                    Log.d(TAG, "✅ Success: Purchase initiated with ID: ${response.data.purchase_id}")

                    // Enhanced logging for bound names in response
                    response.data.tickets_preview?.tickets?.let { tickets ->
                        Log.d(TAG, "📝 Tickets preview with bound names:")
                        tickets.forEach { ticket ->
                            Log.d(TAG, "   Ticket ${ticket.ticket_number}: ${ticket.bound_name}")
                        }
                    }

                    response.data.summary.bound_names?.let { boundNames ->
                        Log.d(TAG, "📋 Summary bound names:")
                        boundNames.forEach { boundName ->
                            Log.d(TAG, "   Ticket ${boundName.ticket_number}: ${boundName.bound_name}")
                        }
                    }

                    Result.success(response.data)
                } else {
                    Log.e(TAG, "❌ Purchase API error: ${response.message}")
                    Result.failure(Exception(response.message))
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Exception during ticket purchase", e)

                // Enhanced error logging
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
                        when (e.code()) {
                            400 -> Result.failure(Exception("Invalid request. Please check your ticket details."))
                            401 -> Result.failure(Exception("Authentication failed. Please log in again."))
                            403 -> Result.failure(Exception("Account not verified. Please complete verification."))
                            404 -> Result.failure(Exception("Event not found or no longer available."))
                            500 -> Result.failure(Exception("Server error. Please try again later."))
                            else -> Result.failure(Exception("Error ${e.code()}: ${e.message()}"))
                        }
                    }
                    else -> Result.failure(e)
                }
            }
        }
    }

    /**
     * Helper function to validate bound names
     * Can be used by UI before making purchase request
     */
    fun validateBoundNames(boundNames: List<String>, quantity: Int): Result<Unit> {
        try {
            // Check count matches quantity
            if (boundNames.size != quantity) {
                return Result.failure(Exception("Please provide exactly $quantity names for your tickets"))
            }

            // Check each name
            boundNames.forEachIndexed { index, name ->
                val trimmedName = name.trim()

                if (trimmedName.isEmpty()) {
                    return Result.failure(Exception("Name for ticket ${index + 1} cannot be empty"))
                }

                if (trimmedName.length > 50) {
                    return Result.failure(Exception("Name for ticket ${index + 1} is too long (maximum 50 characters)"))
                }

                // Check for invalid characters (optional)
                if (!trimmedName.matches(Regex("^[a-zA-Z0-9\\s\\-\\.,']+$"))) {
                    return Result.failure(Exception("Name for ticket ${index + 1} contains invalid characters"))
                }
            }

            // Check for duplicates
            val uniqueNames = boundNames.map { it.trim() }.distinct()
            if (uniqueNames.size != boundNames.size) {
                return Result.failure(Exception("Each ticket must have a unique name"))
            }

            return Result.success(Unit)
        } catch (e: Exception) {
            return Result.failure(Exception("Validation error: ${e.message}"))
        }
    }

    /**
     * Helper function to create PurchaseRequest with bound names
     * Makes it easier to construct the request from UI
     */
    fun createPurchaseRequest(
        eventId: String,
        quantity: Int,
        boundNames: List<String>,
        deviceInfo: DeviceInfo? = null
    ): PurchaseRequest {
        // Trim all bound names
        val trimmedNames = boundNames.map { it.trim() }

        return PurchaseRequest(
            event_id = eventId,
            quantity = quantity,
            bound_names = trimmedNames,
            device_info = deviceInfo
        )
    }

    /**
     * Verify payment status
     * Calls GET /payments/verify endpoint
     */
    suspend fun verifyPayment(
        paymentId: String,
        paypalOrderId: String? = null
    ): Result<PaymentVerificationData> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Making API call to verify payment: $paymentId")

                val response = apiService.verifyPayment(
                    paymentId = paymentId,
                    paypalOrderId = paypalOrderId
                )

                Log.d(TAG, "Received verification response status: ${response.status}, message: ${response.message}")

                if (response.status == "success") {
                    val data = response.data
                    Log.d(TAG, "Success: Payment status: ${data.payment_status}, Tickets ready: ${data.tickets_ready}")
                    Result.success(data)
                } else {
                    Log.e(TAG, "Payment verification API error: ${response.message}")
                    Result.failure(Exception(response.message))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during payment verification", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Get pending purchase info from local storage
     */
    fun getPendingPurchaseInfo(context: Context): PendingPurchaseInfo? {
        return try {
            val prefs = context.getSharedPreferences("purchase_info", Context.MODE_PRIVATE)
            val purchaseId = prefs.getString("pending_purchase_id", null)
            val paymentId = prefs.getString("pending_payment_id", null)

            if (purchaseId != null && paymentId != null) {
                PendingPurchaseInfo(
                    purchaseId = purchaseId,
                    paymentId = paymentId,
                    eventId = prefs.getString("event_id", null),
                    eventName = prefs.getString("event_name", null),
                    quantity = prefs.getInt("quantity", 0),
                    totalAmount = prefs.getFloat("total_amount", 0f).toDouble(),
                    timestamp = prefs.getLong("purchase_timestamp", 0)
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting pending purchase info", e)
            null
        }
    }

    /**
     * Clear pending purchase info from local storage
     */
    fun clearPendingPurchaseInfo(context: Context) {
        try {
            val prefs = context.getSharedPreferences("purchase_info", Context.MODE_PRIVATE)
            prefs.edit().clear().apply()
            Log.d(TAG, "Cleared pending purchase info")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing pending purchase info", e)
        }
    }

    /**
     * Check if there's an expired pending purchase (older than 15 minutes)
     */
    fun hasExpiredPendingPurchase(context: Context): Boolean {
        val pendingInfo = getPendingPurchaseInfo(context)
        return if (pendingInfo != null) {
            val currentTime = System.currentTimeMillis()
            val ageInMinutes = (currentTime - pendingInfo.timestamp) / (1000 * 60)
            ageInMinutes > 15 // PayPal checkout typically expires after 15 minutes
        } else {
            false
        }
    }
}

/**
 * Data class to hold pending purchase information
 */
data class PendingPurchaseInfo(
    val purchaseId: String,
    val paymentId: String,
    val eventId: String?,
    val eventName: String?,
    val quantity: Int,
    val totalAmount: Double,
    val timestamp: Long
)