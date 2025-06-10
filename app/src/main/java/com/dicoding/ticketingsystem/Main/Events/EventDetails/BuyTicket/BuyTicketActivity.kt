package com.dicoding.ticketingsystem.Main.Events.EventDetails.BuyTicket

import android.content.Intent
import android.content.pm.PackageInfo
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.dicoding.ticketingsystem.DataSource.Repository.PurchaseRepository
import com.dicoding.ticketingsystem.DataSource.Response.DeviceInfo
import com.dicoding.ticketingsystem.DataSource.Response.Event
import com.dicoding.ticketingsystem.DataSource.Response.PurchaseData
import com.dicoding.ticketingsystem.DataSource.Response.PurchaseRequest
import com.dicoding.ticketingsystem.R
import com.dicoding.ticketingsystem.databinding.ActivityBuyTicketBinding
import com.google.gson.Gson
import kotlinx.coroutines.launch
import kotlin.math.min

class BuyTicketActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBuyTicketBinding
    private lateinit var event: Event
    private lateinit var purchaseRepository: PurchaseRepository

    private var currentQuantity = 1
    private var maxQuantity = 10
    private var unitPrice = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityBuyTicketBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize repository
        purchaseRepository = PurchaseRepository(this)

        // Get event data from intent
        val eventJson = intent.getStringExtra(EXTRA_EVENT)
        if (eventJson != null) {
            val gson = Gson()
            event = gson.fromJson(eventJson, Event::class.java)

            setupUI()
            setupQuantityControls()
            setupPurchaseButton()
        } else {
            finish()
        }

        setupBackButton()
    }

    private fun setupUI() {
        // Load event thumbnail
        val imageUrl = getEventImageUrl(event)
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.image_empty)
                .error(R.drawable.image_empty)
                .into(binding.ivEventThumbnail)
        }

        // Set event details
        binding.tvEventName.text = getEventName(event)
        binding.tvEventDate.text = formatEventDate(event.date)
        binding.tvEventVenue.text = event.venue

        // Set unit price
        unitPrice = event.price

        // Setup availability and limits
        setupAvailabilityStatus()

        // Initial price calculation
        updatePriceBreakdown()
    }

    private fun setupAvailabilityStatus() {
        binding.apply {
            when {
                event.is_sold_out -> {
                    // Sold out state
                    availabilityCard.backgroundTintList = ContextCompat.getColorStateList(this@BuyTicketActivity, R.color.red)
                    tvAvailabilityStatus.text = "Sold Out"
                    tvTicketsCount.text = "0 / ${event.total}"

                    // Show sold out warning
                    warningCard.visibility = View.VISIBLE
                    warningCard.setCardBackgroundColor(ContextCompat.getColor(this@BuyTicketActivity, R.color.red))
                    tvWarningMessage.text = "Sorry, this event is sold out"

                    // Disable purchase
                    maxQuantity = 0
                    currentQuantity = 0
                    updateQuantityDisplay()

                }
            }
        }
    }

    private fun setupQuantityControls() {
        updateQuantityDisplay()

        binding.btnDecrease.setOnClickListener {
            if (currentQuantity > 1) {
                currentQuantity--
                updateQuantityDisplay()
                updatePriceBreakdown()
            }
        }

        binding.btnIncrease.setOnClickListener {
            if (currentQuantity < maxQuantity) {
                currentQuantity++
                updateQuantityDisplay()
                updatePriceBreakdown()
            }
        }
    }

    private fun updateQuantityDisplay() {
        binding.tvQuantity.text = currentQuantity.toString()

        // Update button states
        binding.btnDecrease.isEnabled = currentQuantity > 1
        binding.btnDecrease.alpha = if (currentQuantity > 1) 1.0f else 0.5f

        binding.btnIncrease.isEnabled = currentQuantity < maxQuantity
        binding.btnIncrease.alpha = if (currentQuantity < maxQuantity) 1.0f else 0.5f
    }

    private fun updatePriceBreakdown() {
        val totalAmount = currentQuantity * unitPrice

        binding.apply {
            tvUnitPriceLabel.text = "$currentQuantity × $${"%.2f".format(unitPrice)}"
            tvUnitPriceAmount.text = "$${"%.2f".format(totalAmount)}"
            tvTotalAmount.text = "$${"%.2f".format(totalAmount)}"

            // Update buy button
            if (event.is_sold_out) {
                btnBuyTickets.text = "SOLD OUT"
                btnBuyTickets.isEnabled = false
                btnBuyTickets.backgroundTintList = ContextCompat.getColorStateList(this@BuyTicketActivity, R.color.dark_grey)
            } else {
                btnBuyTickets.text = "Buy $currentQuantity Ticket${if (currentQuantity > 1) "s" else ""}"
                btnBuyTickets.isEnabled = true
                btnBuyTickets.backgroundTintList = ContextCompat.getColorStateList(this@BuyTicketActivity, R.color.orange_100)
            }
        }
    }

    private fun setupPurchaseButton() {
        binding.btnBuyTickets.setOnClickListener {
            if (!event.is_sold_out && currentQuantity > 0) {
                initiateTicketPurchase()
            }
        }
    }

    // 🔧 FIXED: Updated to use corrected repository pattern
    private fun initiateTicketPurchase() {
        showLoading(true)

        val purchaseRequest = PurchaseRequest(
            event_id = event.id,
            quantity = currentQuantity,
            device_info = DeviceInfo(
                platform = "android",
                app_version = getAppVersion(),
                device_id = getAndroidDeviceId()
            )
        )

        lifecycleScope.launch {
            val result = purchaseRepository.purchaseTickets(purchaseRequest)

            result.fold(
                onSuccess = { purchaseData ->
                    handlePurchaseSuccess(purchaseData)
                },
                onFailure = { error ->
                    handlePurchaseError(error.message ?: "Unknown error occurred")
                }
            )

            showLoading(false)
        }
    }

    private fun handlePurchaseSuccess(purchaseData: PurchaseData) {
        // Save purchase info for verification later
        savePurchaseInfo(purchaseData.purchase_id, purchaseData.payment_id)

        // Show success message
        Toast.makeText(this, "Redirecting to PayPal...", Toast.LENGTH_SHORT).show()

        // Open PayPal checkout
        val paymentData = purchaseData.payment
        openPayPalCheckout(
            checkoutUrl = paymentData.checkout_url,
            mobileDeepLink = paymentData.mobile_deep_links.android,
            fallbackUrl = paymentData.mobile_deep_links.fallback
        )
    }

    private fun handlePurchaseError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()

        // Check if it's availability issue and refresh event data
        if (message.contains("no longer available", ignoreCase = true) ||
            message.contains("sold out", ignoreCase = true)) {
            refreshEventAvailability()
        }
    }

    private fun refreshEventAvailability() {
        // TODO: Call events API to get updated availability
        // For now, just show a message
        Toast.makeText(this, "Please check event details for updated availability", Toast.LENGTH_SHORT).show()
    }

    private fun openPayPalCheckout(checkoutUrl: String, mobileDeepLink: String, fallbackUrl: String) {
        try {
            // Try PayPal app deep link first
            val paypalIntent = Intent(Intent.ACTION_VIEW, Uri.parse(mobileDeepLink))
            paypalIntent.setPackage("com.paypal.android.p2pmobile")

            if (paypalIntent.resolveActivity(packageManager) != null) {
                startActivity(paypalIntent)
            } else {
                // Try generic intent with fallback URL
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl))
                startActivity(webIntent)
            }

            // Don't finish this activity yet - user might return via app switching

        } catch (e: Exception) {
            Toast.makeText(this, "Error opening PayPal: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun savePurchaseInfo(purchaseId: String, paymentId: String) {
        val prefs = getSharedPreferences("purchase_info", MODE_PRIVATE)
        prefs.edit()
            .putString("pending_purchase_id", purchaseId)
            .putString("pending_payment_id", paymentId)
            .putString("event_id", event.id)
            .putString("event_name", getEventName(event))
            .putInt("quantity", currentQuantity)
            .putFloat("total_amount", (currentQuantity * unitPrice).toFloat())
            .putLong("purchase_timestamp", System.currentTimeMillis())
            .apply()
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnBuyTickets.isEnabled = !show

        // Optionally disable quantity controls during loading
        binding.btnDecrease.isEnabled = !show && currentQuantity > 1
        binding.btnIncrease.isEnabled = !show && currentQuantity < maxQuantity
    }

    private fun setupBackButton() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    // Helper functions
    private fun getEventName(event: Event): String {
        return when {
            !event.name.isNullOrEmpty() -> event.name
            !event.event_name.isNullOrEmpty() -> event.event_name!!
            else -> "Untitled Event"
        }
    }

    private fun getEventImageUrl(event: Event): String? {
        return when {
            !event.image.isNullOrEmpty() -> event.image
            !event.event_image_url.isNullOrEmpty() -> event.event_image_url
            else -> null
        }
    }

    private fun formatEventDate(dateString: String): String {
        // Your existing date formatting logic
        return dateString // Simplified for this example
    }

    private fun getAppVersion(): String {
        return try {
            val pInfo: PackageInfo = packageManager.getPackageInfo(packageName, 0)
            pInfo.versionName ?: "1.0.0"  // Handle null case
        } catch (e: Exception) {
            "1.0.0"
        }
    }

    fun getAndroidDeviceId(): String {
        return Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: "unknown_device"  // Handle null case + proper import
    }

    companion object {
        const val EXTRA_EVENT = "extra_event"
    }
}