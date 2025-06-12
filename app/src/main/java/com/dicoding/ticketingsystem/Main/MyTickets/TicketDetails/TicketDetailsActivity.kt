package com.dicoding.ticketingsystem.Main.MyTickets.TicketDetails

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.ticketingsystem.DataSource.Response.TicketGroup
import com.dicoding.ticketingsystem.R
import com.dicoding.ticketingsystem.databinding.ActivityTicketDetailsBinding
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class TicketDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTicketDetailsBinding
    private lateinit var ticketGroup: TicketGroup
    private lateinit var qrAdapter: TicketQrAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Use view binding
        binding = ActivityTicketDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Get ticket group data from intent (using JSON deserialization)
        val ticketGroupJson = intent.getStringExtra(EXTRA_TICKET_GROUP)
        if (ticketGroupJson != null) {
            val gson = com.google.gson.Gson()
            ticketGroup = gson.fromJson(ticketGroupJson, TicketGroup::class.java)

            // Set up the UI with the data
            setupUI()

            // Set up the recycler view for QR codes
            setupQrRecyclerView()

            // Set up the show/hide button
            setupShowHideButton()
        } else {
            // Handle error case - no ticket data
            finish()
            return
        }
    }

    private fun setupUI() {
        val parentTicket = ticketGroup.parent
        val event = parentTicket.event  // Updated: now using 'event' instead of 'events'
        val purchase = parentTicket.purchase_info  // Updated: now using 'purchase_info'

        // Set quantity
        binding.tvQuantity.text = "${ticketGroup.total_in_group} Person"

        // Set event name
        binding.tvEventName.text = event.name  // Updated: now using 'name' instead of 'event_name'

        // Format and set the date
        try {
            // Parse the date - handling ISO 8601 format
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")

            // Clean the date string (remove timezone info)
            val cleanDateString = if (event.date.contains("+") || event.date.contains("Z")) {
                event.date.split("+")[0].split("Z")[0]
            } else {
                event.date
            }

            val date = dateFormat.parse(cleanDateString)

            if (date != null) {
                // Format time (12-hour format)
                val timeFormat = SimpleDateFormat("h a", Locale.US)
                val time = timeFormat.format(date)

                // Format day with suffix
                val dayFormat = SimpleDateFormat("d", Locale.US)
                val day = dayFormat.format(date).toInt()
                val dayWithSuffix = when {
                    day in 11..13 -> "${day}th" // Special case for 11th, 12th, 13th
                    day % 10 == 1 -> "${day}st"
                    day % 10 == 2 -> "${day}nd"
                    day % 10 == 3 -> "${day}rd"
                    else -> "${day}th"
                }

                // Format month and year
                val monthFormat = SimpleDateFormat("MMMM", Locale.US)
                val month = monthFormat.format(date)

                val yearFormat = SimpleDateFormat("yyyy", Locale.US)
                val year = yearFormat.format(date)

                binding.tvEventDate.text = "$time, $dayWithSuffix of $month $year"
            } else {
                binding.tvEventDate.text = "Unknown Date"
            }
        } catch (e: Exception) {
            binding.tvEventDate.text = "Unknown Date"
        }

        // Set venue
        binding.tvEventVenue.text = event.venue

        // Set blockchain ticket ID
        val blockchainId = parentTicket.ticket_info.blockchain_id ?: "N/A"
        binding.tvBlockchainTicketId.text = "NFT Ticket ID: $blockchainId"

        // Set payment amount - format as currency
        val amount = purchase.amount_paid
        binding.tvPaymentAmount.text = "Amount: $${String.format("%.2f", amount)}"

        // Set payment status with conditional formatting
        val paymentStatus = purchase.payment_status ?: "pending"
        val formattedStatus = paymentStatus.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }

        binding.tvStatus.text = formattedStatus

        // Update status color based on payment status
        when (paymentStatus.lowercase()) {
            "confirmed" -> {
                binding.tvStatus.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.green)
            }
            "pending" -> {
                binding.tvStatus.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.orange)
            }
            else -> {
                binding.tvStatus.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.red)
            }
        }
    }

    private fun setupQrRecyclerView() {
        // Create list with both parent and children tickets
        val allTickets = mutableListOf(ticketGroup.parent)
        allTickets.addAll(ticketGroup.children)

        // Setup RecyclerView
        binding.rvQr.apply {
            layoutManager = LinearLayoutManager(this@TicketDetailsActivity)
            adapter = TicketQrAdapter(allTickets).also { qrAdapter = it }
            isNestedScrollingEnabled = false  // Ensure this is set to false
            visibility = View.GONE  // Initially hidden
        }
    }

    private fun setupShowHideButton() {
        // Initially tickets are hidden
        var ticketsVisible = false

        binding.btnShow.setOnClickListener {
            ticketsVisible = !ticketsVisible

            if (ticketsVisible) {
                // Show tickets
                binding.rvQr.visibility = View.VISIBLE
                binding.tvShow.text = "Hide Tickets"

                // Rotate arrows using their IDs
                binding.ivArrow1.rotation = 270f
                binding.ivArrow2.rotation = 270f
            } else {
                // Hide tickets
                binding.rvQr.visibility = View.GONE
                binding.tvShow.text = "Show Tickets"

                // Reset arrow rotation
                binding.ivArrow1.rotation = 90f
                binding.ivArrow2.rotation = 90f
            }
        }
    }

    companion object {
        const val EXTRA_TICKET_GROUP = "extra_ticket_group"
    }
}