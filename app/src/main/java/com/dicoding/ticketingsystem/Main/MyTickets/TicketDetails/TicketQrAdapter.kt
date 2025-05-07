package com.dicoding.ticketingsystem.Main.MyTickets.TicketDetails

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.ticketingsystem.DataSource.Response.Ticket
import com.dicoding.ticketingsystem.databinding.ItemTicketQrcodeBinding

class TicketQrAdapter(
    private val tickets: List<Ticket>
) : RecyclerView.Adapter<TicketQrAdapter.QrViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QrViewHolder {
        val binding = ItemTicketQrcodeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return QrViewHolder(binding)
    }

    override fun getItemCount(): Int = tickets.size

    override fun onBindViewHolder(holder: QrViewHolder, position: Int) {
        holder.bind(tickets[position], position + 1)
    }

    inner class QrViewHolder(private val binding: ItemTicketQrcodeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(ticket: Ticket, position: Int) {
            // Set ticket number
            binding.tvTicketTitle.text = "Ticket #$position"

            // Set ticket ID
            binding.tvTicketId.text = "Ticket ID: ${ticket.ticket_id}"

            // Here you could also set QR code image if you have real QR code data
            // For example with a library like ZXing:
            // val qrBitmap = QRCodeGenerator.generateQRCode(ticket.qr_code_hash)
            // binding.ivQr.setImageBitmap(qrBitmap)
        }
    }
}