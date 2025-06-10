package com.dicoding.ticketingsystem.Main.Events.EventDetails

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.dicoding.ticketingsystem.DataSource.Response.Event
import com.dicoding.ticketingsystem.Main.Events.EventDetails.BuyTicket.BuyTicketActivity
import com.dicoding.ticketingsystem.R
import com.dicoding.ticketingsystem.databinding.ActivityEventDetailsBinding
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class EventDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEventDetailsBinding
    private lateinit var event: Event

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Use view binding
        binding = ActivityEventDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get event data from intent
        val eventJson = intent.getStringExtra(EXTRA_EVENT)
        if (eventJson != null) {
            val gson = Gson()
            event = gson.fromJson(eventJson, Event::class.java)

            // Set up the UI with the data
            setupUI()

            // Set up back button
            setupBackButton()

            // Set up buy button
            setupBuyButton()
        } else {
            // Handle error case
            finish()
        }
    }

    private fun setupUI() {
        // 🔧 FIXED: Load event image with enhanced API fields
        val imageUrl = getEventImageUrl(event)
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.image_empty)
                .error(R.drawable.image_empty)
                .into(binding.ivEventImage)
        }

        // 🔧 FIXED: Set category with enhanced API
        val category = getEventCategory(event)
        binding.tvCategory.text = category

        // 🔧 FIXED: Set event name with enhanced API
        binding.tvTitle.text = getEventName(event)

        // 🔧 FIXED: Format and set event date with enhanced API
        binding.tvEventDate.text = formatEventDate(getEventDate(event))

        // 🔧 FIXED: Set venue
        binding.tvEventVenue.text = event.venue

        // 🔧 FIXED: Set description with enhanced API
        binding.tvDescription.text = getEventDescription(event)

        // 🆕 NEW: Set up ticket availability display
        setupTicketAvailability()
    }

    // 🆕 NEW: Set up ticket availability section
    private fun setupTicketAvailability() {
        binding.apply {
            when {
                // Check if we have ticket data from enhanced API
                event.total > 0 -> {
                    // Show availability count
                    tvEventTicketsCount.text = "${event.available} / ${event.total}"
                    tvEventTicketsCount.setTextColor(resources.getColor(R.color.white, null))

                    // Update availability section based on status
                    when {
                        event.is_sold_out -> {
                            tvEventTicketDetails.text = "Sold Out"
                            // Change background to indicate sold out status
                            binding.linearLayout4.getChildAt(0).setBackgroundResource(R.drawable.background_item)
                            // You could change the tint to red here if you have that color
                        }
                        event.available <= (event.total * 0.1) -> {
                            tvEventTicketDetails.text = "Limited Tickets"
                            tvEventTicketsCount.text = "Only ${event.available} left!"
                        }
                        else -> {
                            tvEventTicketDetails.text = "Tickets Available"
                        }
                    }

                    // 🆕 NEW: Update buy button based on availability
                    updateBuyButton()
                }
                // Fallback for events without ticket data
                else -> {
                    tvEventTicketDetails.text = "Tickets Available"
                    tvEventTicketsCount.text = "TBA"
                    tvEventTicketsCount.setTextColor(resources.getColor(R.color.white, null))
                }
            }
        }
    }

    // 🆕 NEW: Update buy button based on ticket availability and pricing
    private fun updateBuyButton() {
        binding.apply {
            when {
                event.is_sold_out -> {
                    btnBuy.text = "SOLD OUT"
                    btnBuy.isEnabled = false
                    btnBuy.setBackgroundResource(R.drawable.background_item)
                    // You could change tint to gray here
                }
                event.price > 0 -> {
                    btnBuy.text = "Buy Tickets @$${event.price}"
                    btnBuy.isEnabled = true
                    btnBuy.setBackgroundResource(R.drawable.background_item)
                    // Keep original orange color
                }
                else -> {
                    btnBuy.text = "Buy Tickets"
                    btnBuy.isEnabled = true
                }
            }
        }
    }

    // 🆕 NEW: Set up buy button click listener
    private fun setupBuyButton() {
        binding.btnBuy.setOnClickListener {
            if (!event.is_sold_out) {
                // Navigate to BuyTicketActivity
                val gson = Gson()
                val intent = Intent(this, BuyTicketActivity::class.java)
                intent.putExtra(BuyTicketActivity.EXTRA_EVENT, gson.toJson(event))
                startActivity(intent)
            } else {
                // Show sold out message
                Toast.makeText(this, "Sorry, this event is sold out", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 🔧 Helper functions to handle both old and new field names
    private fun getEventName(event: Event): String {
        return when {
            !event.name.isNullOrEmpty() -> event.name
            !event.event_name.isNullOrEmpty() -> event.event_name!!
            else -> "Untitled Event"
        }
    }

    private fun getEventDescription(event: Event): String {
        return when {
            !event.description.isNullOrEmpty() -> event.description!!
            !event.event_description.isNullOrEmpty() -> event.event_description!!
            else -> "No description available"
        }
    }

    private fun getEventCategory(event: Event): String {
        val categoryText = when {
            !event.category.isNullOrEmpty() -> event.category!!
            else -> "General"
        }

        return if (categoryText.isNotEmpty()) {
            categoryText.substring(0, 1).uppercase() + categoryText.substring(1).lowercase()
        } else {
            "General"
        }
    }

    private fun getEventDate(event: Event): String {
        return when {
            !event.date.isNullOrEmpty() -> event.date
            !event.event_date.isNullOrEmpty() -> event.event_date!!
            else -> ""
        }
    }

    private fun getEventImageUrl(event: Event): String? {
        return when {
            !event.image.isNullOrEmpty() -> event.image
            !event.event_image_url.isNullOrEmpty() -> event.event_image_url
            else -> null
        }
    }

    // 🔧 ENHANCED: Better date formatting for enhanced API
    private fun formatEventDate(dateString: String): String {
        if (dateString.isEmpty()) return "Unknown Date"

        return try {
            // Handle timezone-aware date string (enhanced API format)
            val dateTimePart = when {
                dateString.contains("+") -> dateString.substring(0, dateString.indexOf("+"))
                dateString.contains("Z") -> dateString.substring(0, dateString.indexOf("Z"))
                else -> dateString
            }

            // Parse the date
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val date = sdf.parse(dateTimePart)

            if (date != null) {
                // Format time (12-hour format)
                val timeFormat = SimpleDateFormat("h a", Locale.US)
                timeFormat.timeZone = TimeZone.getDefault() // Local time
                val time = timeFormat.format(date)

                // Format day with ordinal suffix
                val dayFormat = SimpleDateFormat("d", Locale.US)
                val day = dayFormat.format(date).toInt()
                val dayWithSuffix = when {
                    day % 10 == 1 && day != 11 -> "${day}st"
                    day % 10 == 2 && day != 12 -> "${day}nd"
                    day % 10 == 3 && day != 13 -> "${day}rd"
                    else -> "${day}th"
                }

                // Get month and year
                val monthFormat = SimpleDateFormat("MMMM", Locale.US)
                val month = monthFormat.format(date)

                val yearFormat = SimpleDateFormat("yyyy", Locale.US)
                val year = yearFormat.format(date)

                "$time, $dayWithSuffix of $month $year"
            } else {
                "Unknown Date"
            }
        } catch (e: Exception) {
            "Unknown Date"
        }
    }

    private fun setupBackButton() {
        binding.btnClose.setOnClickListener {
            finish()
        }
    }

    companion object {
        const val EXTRA_EVENT = "extra_event"
    }
}