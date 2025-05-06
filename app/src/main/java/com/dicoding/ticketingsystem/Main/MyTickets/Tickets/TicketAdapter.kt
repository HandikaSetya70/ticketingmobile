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
import java.text.SimpleDateFormat
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
            val event = parentTicket.events

            // Set up category
            binding.tvCategory.text = event?.category ?: "Unknown"

            // Set up status with conditional formatting
            binding.tvStatus.apply {
                text = when (parentTicket.ticket_status) {
                    "valid" -> "Valid"
                    else -> "Pending"
                }

                if (parentTicket.ticket_status == "valid") {
                    backgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(context, R.color.green_accent)
                    )
                    setTextColor(ContextCompat.getColor(context, R.color.green))
                } else {
                    backgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(context, R.color.red_accent)
                    )
                    setTextColor(ContextCompat.getColor(context, R.color.red))
                }
            }

            // Set event name
            binding.tvTicketTitle.text = event?.event_name ?: "Unknown Event"

            // Set quantity with formatting
            binding.tvQuantity.text = "${ticketGroup.total_in_group} Person"

            // Format and set the date
            val dateText = if (event?.event_date != null) {
                try {
                    // Updated format pattern to match "2025-12-10T19:30:00+00:00"
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
                    dateFormat.timeZone = TimeZone.getTimeZone("UTC")

                    // Extract just the date part before trying to parse
                    val datePart = event.event_date.split("T")[0] + "T" + event.event_date.split("T")[1].split("+")[0]
                    val date = dateFormat.parse(datePart)

                    val dayFormat = SimpleDateFormat("d", Locale.US)
                    val day = dayFormat.format(date)

                    // Add ordinal suffix (1st, 2nd, 3rd, etc.)
                    val dayWithSuffix = when {
                        day.endsWith("1") && day != "11" -> "${day}st"
                        day.endsWith("2") && day != "12" -> "${day}nd"
                        day.endsWith("3") && day != "13" -> "${day}rd"
                        else -> "${day}th"
                    }

                    val monthFormat = SimpleDateFormat("MMMM", Locale.US)
                    val month = monthFormat.format(date)

                    val yearFormat = SimpleDateFormat("yyyy", Locale.US)
                    val year = yearFormat.format(date)

                    "Event date: $dayWithSuffix of $month $year"
                } catch (e: Exception) {
                    Log.e("TicketAdapter", "Error parsing date: ${event.event_date}", e)
                    "Event date: Unknown"
                }
            } else {
                "Event date: Unknown"
            }
            binding.tvDate.text = dateText

            // Set click listener
            itemView.setOnClickListener {
                onTicketClicked(ticketGroup)
            }
        }
    }
}