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
                // Set title and description
                tvTitle.text = event.event_name
                tvDescription.text = event.event_description ?: "No description available"

                // Format category (first letter uppercase, rest lowercase)
                val category = event.category?.let {
                    if (it.isNotEmpty()) {
                        it.substring(0, 1).uppercase() + it.substring(1).lowercase()
                    } else {
                        "General"
                    }
                } ?: "General"
                tvCategory.text = category

                // Format date
                val dateText = formatEventDate(event.event_date)
                tvDate.text = dateText

                // Load image with Glide
                if (!event.event_image_url.isNullOrEmpty()) {
                    Glide.with(itemView.context)
                        .load(event.event_image_url)
                        .placeholder(R.drawable.image_empty)
                        .error(R.drawable.image_empty)
                        .into(ivImage)
                }
            }
        }

        private fun formatEventDate(dateString: String?): String {
            if (dateString.isNullOrEmpty()) return "Event date: Unknown"

            return try {
                // Remove timezone part if exists
                val dateTimePart = if (dateString.contains("+")) {
                    dateString.substring(0, dateString.indexOf("+"))
                } else if (dateString.contains("Z")) {
                    dateString.substring(0, dateString.indexOf("Z"))
                } else {
                    dateString
                }

                // Parse the date
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                val date = sdf.parse(dateTimePart)

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
            } catch (e: Exception) {
                "Event date: Unknown"
            }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Event>() {
            override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
                return oldItem.event_id == newItem.event_id
            }

            override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
                return oldItem == newItem
            }
        }
    }
}