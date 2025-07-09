package com.dicoding.ticketingsystem.Main.Events.EventDetails.BuyTicket

import android.content.Intent
import android.content.pm.PackageInfo
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
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

class BuyTicketActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBuyTicketBinding
    private lateinit var event: Event
    private lateinit var purchaseRepository: PurchaseRepository

    private var currentQuantity = 1
    private var maxQuantity = 10
    private var unitPrice = 0.0

    // NEW: List to hold bound name EditTexts
    private val boundNameEditTexts = mutableListOf<EditText>()

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
            setupBoundNamesSection() // NEW
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
                updateBoundNamesFields() // NEW
            }
        }

        binding.btnIncrease.setOnClickListener {
            if (currentQuantity < maxQuantity) {
                currentQuantity++
                updateQuantityDisplay()
                updatePriceBreakdown()
                updateBoundNamesFields() // NEW
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

    // NEW: Setup bound names section
    private fun setupBoundNamesSection() {
        updateBoundNamesFields()
    }

    // NEW: Update bound names input fields based on quantity
    private fun updateBoundNamesFields() {
        val container = binding.containerBoundNames

        // Clear existing views
        container.removeAllViews()
        boundNameEditTexts.clear()

        // Create input fields for each ticket
        for (i in 1..currentQuantity) {
            val nameInputLayout = createBoundNameInputField(i)
            container.addView(nameInputLayout)
        }

        // Update purchase button state
        updatePurchaseButtonState()
    }

    // NEW: Create individual bound name input field
    private fun createBoundNameInputField(ticketNumber: Int): View {
        val context = this

        // Create container
        val containerLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dpToPx(12)
            }
        }

        // Create label
        val label = TextView(context).apply {
            text = "Ticket $ticketNumber Holder Name"
            setTextColor(ContextCompat.getColor(context, R.color.black))
            textSize = 14f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dpToPx(8)
            }
        }

        // Create EditText
        val editText = EditText(context).apply {
            hint = "Enter full name or initials"
            setHintTextColor(ContextCompat.getColor(context, R.color.dark_grey))
            setTextColor(ContextCompat.getColor(context, R.color.black))
            textSize = 14f
            setPadding(dpToPx(16), dpToPx(12), dpToPx(16), dpToPx(12))
            background = ContextCompat.getDrawable(context, R.drawable.background_item)
            backgroundTintList = ContextCompat.getColorStateList(context, R.color.green_accent)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            // Add text watcher to validate input
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    validateBoundNameInput(this@apply, s.toString())
                    updatePurchaseButtonState()
                }
            })
        }

        // Add to tracking list
        boundNameEditTexts.add(editText)

        // Add views to container
        containerLayout.addView(label)
        containerLayout.addView(editText)

        return containerLayout
    }

    // NEW: Validate individual bound name input
    private fun validateBoundNameInput(editText: EditText, text: String) {
        when {
            text.length > 50 -> {
                editText.error = "Name too long (max 50 characters)"
            }
            text.isNotBlank() && !text.matches(Regex("^[a-zA-Z0-9\\s\\-\\.,']+$")) -> {
                editText.error = "Invalid characters in name"
            }
            else -> {
                editText.error = null
            }
        }
    }

    // NEW: Get bound names from input fields
    private fun getBoundNames(): List<String> {
        return boundNameEditTexts.map { it.text.toString().trim() }
    }

    // NEW: Validate all bound names
    private fun validateAllBoundNames(): Pair<Boolean, String?> {
        val boundNames = getBoundNames()

        // Check if all fields are filled
        boundNames.forEachIndexed { index, name ->
            if (name.isEmpty()) {
                return Pair(false, "Please enter name for ticket ${index + 1}")
            }
            if (name.length > 50) {
                return Pair(false, "Name for ticket ${index + 1} is too long (max 50 characters)")
            }
        }

        // Check for duplicates
        val uniqueNames = boundNames.distinct()
        if (uniqueNames.size != boundNames.size) {
            return Pair(false, "Each ticket must have a unique name")
        }

        return Pair(true, null)
    }

    // NEW: Update purchase button state based on validation
    private fun updatePurchaseButtonState() {
        val (isValid, errorMessage) = validateAllBoundNames()

        binding.btnBuyTickets.isEnabled = isValid && !event.is_sold_out && currentQuantity > 0

        if (!isValid && errorMessage != null) {
            // Show validation error in button or hint
            binding.tvBoundNamesHint.text = "⚠️ $errorMessage"
            binding.tvBoundNamesHint.setTextColor(ContextCompat.getColor(this, R.color.red))
        } else {
            // Reset hint to default
            binding.tvBoundNamesHint.text = "💡 Use full names or initials (e.g., 'John Doe' or 'J.D.')"
            binding.tvBoundNamesHint.setTextColor(ContextCompat.getColor(this, R.color.dark_grey))
        }
    }

    // Helper function to convert dp to pixels
    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
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
                btnBuyTickets.text = "Buy $currentQuantity Ticket${if (currentQuantity > 1) "s" else ""} - ${"%.2f".format(totalAmount)}"
                // Button state will be updated by updatePurchaseButtonState()
                btnBuyTickets.backgroundTintList = ContextCompat.getColorStateList(this@BuyTicketActivity, R.color.orange_100)
            }
        }
    }

    private fun setupPurchaseButton() {
        binding.btnBuyTickets.setOnClickListener {
            if (!event.is_sold_out && currentQuantity > 0) {
                // NEW: Validate bound names before proceeding
                val (isValid, errorMessage) = validateAllBoundNames()
                if (!isValid) {
                    Toast.makeText(this, errorMessage ?: "Please fill in all ticket holder names", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                initiateTicketPurchase()
            }
        }
    }

    // UPDATED: Modified to include bound names
    private fun initiateTicketPurchase() {
        showLoading(true)

        // Get bound names from input fields
        val boundNames = getBoundNames()

        // Log bound names for debugging
        boundNames.forEachIndexed { index, name ->
            android.util.Log.d("BuyTicketActivity", "Ticket ${index + 1}: $name")
        }

        // UPDATED: Create purchase request with bound names
        val purchaseRequest = PurchaseRequest(
            event_id = event.id,
            quantity = currentQuantity,
            bound_names = boundNames, // NEW: Include bound names
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

        // NEW: Log bound names from response for verification
        purchaseData.tickets_preview?.tickets?.forEach { ticket ->
            android.util.Log.d("BuyTicketActivity", "Response - Ticket ${ticket.ticket_number}: ${ticket.bound_name}")
        }

        // Show success message with bound names preview
        val boundNamesPreview = purchaseData.tickets_preview?.tickets?.joinToString(", ") { it.bound_name } ?: "tickets"
        Toast.makeText(this, "Tickets for $boundNamesPreview - Redirecting to PayPal...", Toast.LENGTH_SHORT).show()

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

        // Check if it's bound names related error
        if (message.contains("bound", ignoreCase = true) ||
            message.contains("name", ignoreCase = true)) {
            // Scroll to bound names section
            binding.containerBoundNames.requestFocus()
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

        // NEW: Save bound names for later reference
        val boundNames = getBoundNames()
        val boundNamesJson = Gson().toJson(boundNames)

        prefs.edit()
            .putString("pending_purchase_id", purchaseId)
            .putString("pending_payment_id", paymentId)
            .putString("event_id", event.id)
            .putString("event_name", getEventName(event))
            .putInt("quantity", currentQuantity)
            .putFloat("total_amount", (currentQuantity * unitPrice).toFloat())
            .putString("bound_names", boundNamesJson) // NEW: Save bound names
            .putLong("purchase_timestamp", System.currentTimeMillis())
            .apply()
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnBuyTickets.isEnabled = !show

        // Disable quantity controls and bound name inputs during loading
        binding.btnDecrease.isEnabled = !show && currentQuantity > 1
        binding.btnIncrease.isEnabled = !show && currentQuantity < maxQuantity

        // NEW: Disable bound name inputs during loading
        boundNameEditTexts.forEach { editText ->
            editText.isEnabled = !show
        }
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