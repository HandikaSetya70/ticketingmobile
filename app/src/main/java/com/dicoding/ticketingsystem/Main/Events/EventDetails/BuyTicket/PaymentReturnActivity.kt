package com.dicoding.ticketingsystem.Main.Events.EventDetails.BuyTicket

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dicoding.ticketingsystem.DataSource.Repository.PurchaseRepository
import com.dicoding.ticketingsystem.DataSource.Response.PaymentVerificationData
import com.dicoding.ticketingsystem.MainActivity
import kotlinx.coroutines.launch

class PaymentReturnActivity : AppCompatActivity() {

    private lateinit var purchaseRepository: PurchaseRepository
    private val TAG = "PaymentReturnActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize repository
        purchaseRepository = PurchaseRepository(this)

        // Handle deep link return from PayPal
        handlePaymentReturn()
    }

    private fun handlePaymentReturn() {
        val data = intent.data

        if (data != null && data.scheme == "ticketapp") {
            Log.d(TAG, "Received deep link: $data")

            when (data.host) {
                "payment-success" -> {
                    val paymentId = data.getQueryParameter("payment_id")
                    if (paymentId != null) {
                        verifyPayment(paymentId)
                    } else {
                        // Try to get payment ID from pending purchase info
                        val pendingInfo = purchaseRepository.getPendingPurchaseInfo(this)
                        if (pendingInfo != null) {
                            verifyPayment(pendingInfo.paymentId)
                        } else {
                            handlePaymentError("Payment ID not found")
                        }
                    }
                }
                "payment-cancel" -> {
                    handlePaymentCancellation()
                }
                else -> {
                    Log.w(TAG, "Unknown deep link host: ${data.host}")
                    finish()
                }
            }
        } else {
            // Check if there's a pending purchase to verify
            checkForPendingPurchase()
        }
    }

    private fun checkForPendingPurchase() {
        val pendingInfo = purchaseRepository.getPendingPurchaseInfo(this)

        if (pendingInfo != null) {
            // Check if the purchase is expired
            if (purchaseRepository.hasExpiredPendingPurchase(this)) {
                showExpiredPurchaseDialog()
            } else {
                // Ask user if they want to check payment status
                showPendingPurchaseDialog(pendingInfo)
            }
        } else {
            // No pending purchase, just go to main activity
            navigateToMain()
        }
    }

    private fun showPendingPurchaseDialog(pendingInfo: com.dicoding.ticketingsystem.DataSource.Repository.PendingPurchaseInfo) {
        AlertDialog.Builder(this)
            .setTitle("Pending Purchase")
            .setMessage("You have a pending purchase for ${pendingInfo.eventName}. Would you like to check the payment status?")
            .setPositiveButton("Check Status") { _, _ ->
                verifyPayment(pendingInfo.paymentId)
            }
            .setNegativeButton("Cancel") { _, _ ->
                navigateToMain()
            }
            .setCancelable(false)
            .show()
    }

    private fun showExpiredPurchaseDialog() {
        AlertDialog.Builder(this)
            .setTitle("Expired Purchase")
            .setMessage("Your previous purchase session has expired. Please try purchasing again if needed.")
            .setPositiveButton("OK") { _, _ ->
                purchaseRepository.clearPendingPurchaseInfo(this)
                navigateToMain()
            }
            .setCancelable(false)
            .show()
    }

    private fun verifyPayment(paymentId: String) {
        // Show loading dialog
        val loadingDialog = ProgressDialog(this).apply {
            setMessage("Verifying payment...")
            setCancelable(false)
            show()
        }

        Log.d(TAG, "Verifying payment: $paymentId")

        lifecycleScope.launch {
            try {
                val result = purchaseRepository.verifyPayment(paymentId)

                result.fold(
                    onSuccess = { verificationData ->
                        handlePaymentVerified(verificationData)
                    },
                    onFailure = { error ->
                        handlePaymentError(error.message ?: "Payment verification failed")
                    }
                )

            } catch (e: Exception) {
                Log.e(TAG, "Exception during payment verification", e)
                handlePaymentError("Verification error: ${e.message}")
            } finally {
                loadingDialog.dismiss()
            }
        }
    }

    private fun handlePaymentVerified(data: PaymentVerificationData) {
        Log.d(TAG, "Payment verification result: status=${data.payment_status}, tickets_ready=${data.tickets_ready}")

        when {
            data.tickets_ready -> {
                // Payment successful, tickets are ready
                purchaseRepository.clearPendingPurchaseInfo(this)
                showSuccessDialog(data)
            }
            data.payment_status == "confirmed" -> {
                // Payment confirmed but tickets still processing
                showProcessingDialog(data)
            }
            data.payment_status == "pending" -> {
                // Payment still pending
                showPendingDialog()
            }
            else -> {
                // Payment failed or cancelled
                handlePaymentError("Payment ${data.payment_status}")
            }
        }
    }

    private fun showSuccessDialog(data: PaymentVerificationData) {
        val eventName = data.event_info?.name ?: "Unknown Event"
        val ticketCount = data.tickets_count

        AlertDialog.Builder(this)
            .setTitle("🎉 Purchase Successful!")
            .setMessage("You've successfully purchased $ticketCount ticket(s) for $eventName. Your tickets are ready to view!")
            .setPositiveButton("View Tickets") { _, _ ->
                navigateToTickets()
            }
            .setNegativeButton("Download Receipt") { _, _ ->
                downloadReceipt(data.receipt?.download_url)
            }
            .setNeutralButton("Later") { _, _ ->
                navigateToMain()
            }
            .setCancelable(false)
            .show()
    }

    private fun showProcessingDialog(data: PaymentVerificationData) {
        AlertDialog.Builder(this)
            .setTitle("Payment Confirmed")
            .setMessage("Your payment has been confirmed! Your tickets are being processed and will be available shortly.")
            .setPositiveButton("Check Later") { _, _ ->
                navigateToMain()
            }
            .setNegativeButton("Download Receipt") { _, _ ->
                downloadReceipt(data.receipt?.download_url)
            }
            .setCancelable(false)
            .show()
    }

    private fun showPendingDialog() {
        AlertDialog.Builder(this)
            .setTitle("Payment Pending")
            .setMessage("Your payment is still being processed. Please check back in a few minutes.")
            .setPositiveButton("Check Later") { _, _ ->
                navigateToMain()
            }
            .setCancelable(false)
            .show()
    }

    private fun handlePaymentCancellation() {
        Log.d(TAG, "Payment was cancelled by user")

        AlertDialog.Builder(this)
            .setTitle("Payment Cancelled")
            .setMessage("Your payment was cancelled. You can try again anytime.")
            .setPositiveButton("OK") { _, _ ->
                navigateToMain()
            }
            .setCancelable(false)
            .show()
    }

    private fun handlePaymentError(message: String) {
        Log.e(TAG, "Payment error: $message")

        Toast.makeText(this, message, Toast.LENGTH_LONG).show()

        // Don't clear pending purchase info in case user wants to retry
        navigateToMain()
    }

    private fun downloadReceipt(downloadUrl: String?) {
        if (!downloadUrl.isNullOrEmpty()) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl))
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "Could not open receipt: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Receipt not available", Toast.LENGTH_SHORT).show()
        }

        navigateToMain()
    }

    private fun navigateToTickets() {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("navigate_to", "tickets")
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
        finish()
    }
}