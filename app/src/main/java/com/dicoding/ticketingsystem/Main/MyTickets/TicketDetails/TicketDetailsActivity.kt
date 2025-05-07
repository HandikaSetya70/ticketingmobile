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

        // Get ticket group data from intent
        val ticketGroupJson = intent.getStringExtra(EXTRA_TICKET_GROUP)
        if (ticketGroupJson != null) {
            val gson = Gson()
            ticketGroup = gson.fromJson(ticketGroupJson, TicketGroup::class.java)

            // Set up the UI with the data
            setupUI()

            // Set up the recycler view for QR codes
            setupQrRecyclerView()

            // Set up the show/hide button
            setupShowHideButton()
        } else {
            // Handle error case
            finish()
        }
    }

    private fun setupUI() {
        val parentTicket = ticketGroup.parent
        val event = parentTicket.events
        val payment = parentTicket.payments

        // Set quantity
        binding.tvQuantity.text = "${ticketGroup.total_in_group} Person"

        // Set event name
        binding.tvEventName.text = event?.event_name ?: "Unknown Event"

        // Format and set the date
        if (event?.event_date != null) {
            try {
                // Parse the date
                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
                dateFormat.timeZone = TimeZone.getTimeZone("UTC")

                // Extract date part
                val datePart = event.event_date.split("T")[0] + "T" + event.event_date.split("T")[1].split("+")[0]
                val date = dateFormat.parse(datePart)

                // Format time
                val timeFormat = SimpleDateFormat("h a", Locale.US)
                val time = timeFormat.format(date)

                // Format day with suffix
                val dayFormat = SimpleDateFormat("d", Locale.US)
                val day = dayFormat.format(date)
                val dayWithSuffix = when {
                    day.endsWith("1") && day != "11" -> "${day}st"
                    day.endsWith("2") && day != "12" -> "${day}nd"
                    day.endsWith("3") && day != "13" -> "${day}rd"
                    else -> "${day}th"
                }

                // Format month and year
                val monthFormat = SimpleDateFormat("MMMM", Locale.US)
                val month = monthFormat.format(date)

                val yearFormat = SimpleDateFormat("yyyy", Locale.US)
                val year = yearFormat.format(date)

                binding.tvEventDate.text = "$time, $dayWithSuffix of $month $year"
            } catch (e: Exception) {
                binding.tvEventDate.text = "Unknown Date"
            }
        } else {
            binding.tvEventDate.text = "Unknown Date"
        }

        // Set venue
        binding.tvEventVenue.text = event?.venue ?: "Unknown Venue"

        // Set blockchain ticket ID
        binding.tvBlockchainTicketId.text = "NFT Ticket ID: ${parentTicket.blockchain_ticket_id}"

        // Set payment amount
        binding.tvPaymentAmount.text = "Amount: $${payment?.amount ?: "0"}"

        // Set payment status with conditional formatting
        val paymentStatus = payment?.payment_status ?: "pending"
        val formattedStatus = paymentStatus.capitalize(Locale.getDefault())

        binding.tvStatus.text = formattedStatus

        if (paymentStatus.equals("confirmed", ignoreCase = true)) {
            binding.tvStatus.backgroundTintList =
                ContextCompat.getColorStateList(this, R.color.green)
        } else {
            binding.tvStatus.backgroundTintList =
                ContextCompat.getColorStateList(this, R.color.red)
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