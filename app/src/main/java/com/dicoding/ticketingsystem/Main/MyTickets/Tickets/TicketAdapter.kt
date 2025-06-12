package com.dicoding.ticketingsystem.Main.MyTickets.Tickets

import android.content.res.ColorStateList
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.ticketingsystem.DataSource.Response.TicketGroup
import com.dicoding.ticketingsystem.R
import com.dicoding.ticketingsystem.databinding.ItemTicketBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Currency
import java.util.Locale
import java.util.TimeZone

class TicketAdapter(
    private val tickets: List<TicketGroup>,
    private val onTicketClicked: (TicketGroup) -> Unit
) : RecyclerView.Adapter<TicketAdapter.TicketViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketViewHolder {
        val binding = ItemTicketBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TicketViewHolder(binding)
    }

    override fun getItemCount(): Int = tickets.size

    override fun onBindViewHolder(holder: TicketViewHolder, position: Int) {
        holder.bind(tickets[position])
    }

    inner class TicketViewHolder(private val binding: ItemTicketBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(ticketGroup: TicketGroup) {
            val parentTicket = ticketGroup.parent
            val event = parentTicket.event

            // Set up category
            binding.tvCategory.text = event.category ?: "Unknown"

            // Set up status with conditional formatting
            binding.tvStatus.apply {
                text = when (parentTicket.status) {
                    "valid" -> "Valid"
                    "revoked" -> "Revoked"
                    "used" -> "Used"
                    else -> "Pending"
                }

                when (parentTicket.status) {
                    "valid" -> {
                        backgroundTintList = ColorStateList.valueOf(
                            ContextCompat.getColor(context, R.color.green_accent)
                        )
                        setTextColor(ContextCompat.getColor(context, R.color.green))
                    }
                    "used" -> {
                        backgroundTintList = ColorStateList.valueOf(
                            ContextCompat.getColor(context, R.color.blue_accent)
                        )
                        setTextColor(ContextCompat.getColor(context, R.color.blue))
                    }
                    "revoked" -> {
                        backgroundTintList = ColorStateList.valueOf(
                            ContextCompat.getColor(context, R.color.red_accent)
                        )
                        setTextColor(ContextCompat.getColor(context, R.color.red))
                    }
                    else -> {
                        backgroundTintList = ColorStateList.valueOf(
                            ContextCompat.getColor(context, R.color.orange_accent)
                        )
                        setTextColor(ContextCompat.getColor(context, R.color.orange))
                    }
                }
            }

            // Set event name
            binding.tvTicketTitle.text = event.name

            // Set quantity with better formatting
            val quantityText = when (ticketGroup.total_in_group) {
                1 -> "1 Person"
                else -> "${ticketGroup.total_in_group} People"
            }
            binding.tvQuantity.text = quantityText

            // Format and set the date with improved parsing
            val dateText = formatEventDate(event.date)
            binding.tvDate.text = dateText

            // Optional: Add price information if available
            if (event.price > 0) {
                val formattedPrice = formatCurrency(event.price)
                binding.tvTicketTitle.text = "${event.name}"
            }

            // Optional: Add event status indicator
            if (parentTicket.validity.is_upcoming) {
                val daysLeft = parentTicket.validity.days_till_event
                if (daysLeft != null && daysLeft <= 7) {
                    // Show urgency for events within a week
                    binding.tvDate.setTextColor(
                        ContextCompat.getColor(binding.root.context, R.color.orange)
                    )
                }
            } else {
                // Past event
                binding.tvDate.setTextColor(
                    ContextCompat.getColor(binding.root.context, R.color.blue_accent)
                )
            }

            // Set click listener
            itemView.setOnClickListener {
                onTicketClicked(ticketGroup)
            }
        }

        private fun formatEventDate(dateString: String): String {
            return try {
                // Handle ISO 8601 format: "2025-12-10T19:30:00+00:00"
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
                inputFormat.timeZone = TimeZone.getTimeZone("UTC")

                // Extract the date part before the timezone
                val cleanDateString = if (dateString.contains("+") || dateString.contains("Z")) {
                    dateString.split("+")[0].split("Z")[0]
                } else {
                    dateString
                }

                val date = inputFormat.parse(cleanDateString)

                if (date != null) {
                    val dayFormat = SimpleDateFormat("d", Locale.US)
                    val day = dayFormat.format(date).toInt()

                    // Add ordinal suffix
                    val dayWithSuffix = when {
                        day in 11..13 -> "${day}th" // Special case for 11th, 12th, 13th
                        day % 10 == 1 -> "${day}st"
                        day % 10 == 2 -> "${day}nd"
                        day % 10 == 3 -> "${day}rd"
                        else -> "${day}th"
                    }

                    val monthFormat = SimpleDateFormat("MMMM", Locale.US)
                    val month = monthFormat.format(date)

                    val yearFormat = SimpleDateFormat("yyyy", Locale.US)
                    val year = yearFormat.format(date)

                    val timeFormat = SimpleDateFormat("HH:mm", Locale.US)
                    val time = timeFormat.format(date)

                    "Event date: $dayWithSuffix of $month $year at $time"
                } else {
                    "Event date: Invalid date format"
                }
            } catch (e: Exception) {
                Log.e("TicketAdapter", "Error parsing date: $dateString", e)
                "Event date: $dateString" // Fallback to original string
            }
        }

        private fun formatCurrency(amount: Double): String {
            return try {
                val format = NumberFormat.getCurrencyInstance(Locale.US)
                format.currency = Currency.getInstance("USD")
                format.format(amount)
            } catch (e: Exception) {
                "$${String.format("%.2f", amount)}"
            }
        }
    }
}