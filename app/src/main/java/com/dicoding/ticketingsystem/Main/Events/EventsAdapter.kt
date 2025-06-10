package com.dicoding.ticketingsystem.Main.Events

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dicoding.ticketingsystem.DataSource.Response.Event
import com.dicoding.ticketingsystem.R
import com.dicoding.ticketingsystem.databinding.ItemEventsBinding
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class EventsAdapter(
    private val onClickEvent: (Event) -> Unit
) : ListAdapter<Event, EventsAdapter.EventViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemEventsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = getItem(position)
        holder.bind(event)
        holder.itemView.setOnClickListener {
            onClickEvent(event)
        }
    }

    inner class EventViewHolder(private val binding: ItemEventsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(event: Event) {
            binding.apply {
                // 🔧 FIXED: Use enhanced API field names with fallbacks
                tvTitle.text = getEventName(event)
                tvDescription.text = getEventDescription(event)

                // 🔧 FIXED: Format category with enhanced API
                val category = getEventCategory(event)
                tvCategory.text = category

                // 🆕 NEW: Update capacity with availability info
                updateCapacityDisplay(event)

                // 🔧 FIXED: Format date with enhanced API
                val dateText = formatEventDate(getEventDate(event))
                tvDate.text = dateText

                // 🔧 FIXED: Load image with enhanced API
                val imageUrl = getEventImageUrl(event)
                if (!imageUrl.isNullOrEmpty()) {
                    Glide.with(itemView.context)
                        .load(imageUrl)
                        .placeholder(R.drawable.image_empty)
                        .error(R.drawable.image_empty)
                        .into(ivImage)
                } else {
                    // Set placeholder if no image
                    ivImage.setImageResource(R.drawable.image_empty)
                }
            }
        }

        // 🆕 NEW: Update capacity display with availability and pricing
        private fun updateCapacityDisplay(event: Event) {
            binding.apply {
                when {
                    // Show availability info if we have ticket data
                    event.total > 0 -> {
                        val availabilityText = "${event.available}/${event.total}"
                        tvCapacity.text = availabilityText

                        // Update background color based on availability
                        when {
                            event.is_sold_out -> {
                                // Sold out - red background
                                tvCapacity.setBackgroundResource(R.drawable.chip_category_orange) // You can create a red variant
                                tvCapacity.text = "SOLD OUT"
                            }
                            event.available <= (event.total * 0.1) -> {
                                // Limited availability (≤10%) - orange background
                                tvCapacity.setBackgroundResource(R.drawable.chip_category_orange)
                                tvCapacity.text = "ONLY $availabilityText LEFT"
                            }
                            else -> {
                                // Good availability - blue background (existing)
                                tvCapacity.setBackgroundResource(R.drawable.chip_category_blue)
                                tvCapacity.text = availabilityText
                            }
                        }
                    }
                    // Fallback for events without ticket data
                    else -> {
                        tvCapacity.text = "TBA"
                        tvCapacity.setBackgroundResource(R.drawable.chip_category_blue)
                    }
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

        private fun formatEventDate(dateString: String?): String {
            if (dateString.isNullOrEmpty()) return "Event date: Unknown"

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
                    // Format time
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

                    "Event date: $time Local Time, $dayWithSuffix of $month $year"
                } else {
                    "Event date: Unknown"
                }
            } catch (e: Exception) {
                "Event date: Unknown"
            }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Event>() {
            override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
                // 🔧 FIXED: Use both id and event_id for comparison
                return (oldItem.id == newItem.id) || (oldItem.event_id == newItem.event_id)
            }

            override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
                return oldItem == newItem
            }
        }
    }
}