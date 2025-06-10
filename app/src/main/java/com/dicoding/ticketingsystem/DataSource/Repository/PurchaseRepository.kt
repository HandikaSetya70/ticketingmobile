package com.dicoding.ticketingsystem.DataSource.Repository

import android.content.Context
import android.util.Log
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
                Log.d(TAG, "Making API call to purchase tickets for event: ${request.event_id}, quantity: ${request.quantity}")

                val response = apiService.purchaseTickets(request)

                Log.d(TAG, "Received purchase response status: ${response.status}, message: ${response.message}")

                if (response.status == "success") {
                    Log.d(TAG, "Success: Purchase initiated with ID: ${response.data.purchase_id}")
                    Result.success(response.data)
                } else {
                    Log.e(TAG, "Purchase API error: ${response.message}")
                    Result.failure(Exception(response.message))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during ticket purchase", e)
                Result.failure(e)
            }
        }
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