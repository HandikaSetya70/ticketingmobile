package com.dicoding.ticketingsystem.Main.Events.EventDetails

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.dicoding.ticketingsystem.DataSource.Response.Event
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
        } else {
            // Handle error case
            finish()
        }
    }

    private fun setupUI() {
        // Load event image with Glide
        if (!event.event_image_url.isNullOrEmpty()) {
            Glide.with(this)
                .load(event.event_image_url)
                .placeholder(R.drawable.image_empty)
                .error(R.drawable.image_empty)
                .into(binding.ivEventImage)
        }

        // Set category with first letter capitalized
        val category = event.category?.let {
            if (it.isNotEmpty()) {
                it.substring(0, 1).uppercase() + it.substring(1).lowercase()
            } else {
                "Unknown"
            }
        } ?: "Unknown"
        binding.tvCategory.text = category

        // Set event name
        binding.tvTitle.text = event.event_name

        // Format and set event date
        binding.tvEventDate.text = formatEventDate(event.event_date)

        // Set venue
        binding.tvEventVenue.text = event.venue

        // Set description
        binding.tvDescription.text = event.event_description ?: "No description available"
    }

    private fun formatEventDate(dateString: String): String {
        return try {
            // Parse the date
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")

            // Extract date part
            val datePart = dateString.split("T")[0] + "T" + dateString.split("T")[1].split("+")[0]
            val date = dateFormat.parse(datePart)

            // Format time
            val timeFormat = SimpleDateFormat("ha", Locale.US)
            var time = timeFormat.format(date)

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

            "$time, $dayWithSuffix of $month $year"
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