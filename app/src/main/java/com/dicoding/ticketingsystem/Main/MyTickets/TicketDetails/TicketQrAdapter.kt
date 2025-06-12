package com.dicoding.ticketingsystem.Main.MyTickets.TicketDetails

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.ticketingsystem.DataSource.Response.TicketData
import com.dicoding.ticketingsystem.databinding.ItemTicketQrcodeBinding

class TicketQrAdapter(
    private val tickets: List<TicketData>  // Updated: now using TicketData instead of Ticket
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
        holder.bind(tickets[position])
    }

    inner class QrViewHolder(private val binding: ItemTicketQrcodeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(ticket: TicketData) {
            // Set ticket number using the actual ticket number from the data
            val ticketNumber = ticket.ticket_info.number
            binding.tvTicketTitle.text = "Ticket #$ticketNumber"

            // Set ticket ID (shortened for better display)
            val shortTicketId = ticket.ticket_id.take(8) + "..."
            binding.tvTicketId.text = "Ticket ID: $shortTicketId"

            // Debug: Log QR code data
            android.util.Log.d("TicketQrAdapter", "QR Code: ${ticket.qr_code}")
            android.util.Log.d("TicketQrAdapter", "QR Data: ${ticket.qr_data}")

            // Handle QR code display
            when {
                // Case 1: QR code is provided as base64 encoded image
                ticket.qr_code != null && ticket.qr_code.isNotEmpty() && !ticket.qr_code.contains("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==") -> {
                    val qrBitmap = decodeBase64ToBitmap(ticket.qr_code)
                    if (qrBitmap != null) {
                        binding.ivQr.setImageBitmap(qrBitmap)
                        android.util.Log.d("TicketQrAdapter", "Set QR from base64")
                    } else {
                        generateQRFromData(ticket.qr_data)
                    }
                }
                // Case 2: Generate QR code from qr_data
                ticket.qr_data.isNotEmpty() -> {
                    generateQRFromData(ticket.qr_data)
                }
                // Case 3: Fallback to placeholder
                else -> {
                    android.util.Log.d("TicketQrAdapter", "Using placeholder QR")
                    // Keep the default drawable
                }
            }
        }

        private fun generateQRFromData(qrData: String) {
            android.util.Log.d("TicketQrAdapter", "Generating QR from data: $qrData")

            // Use the improved QR generator
            try {
                val qrBitmap = com.dicoding.ticketingsystem.DataSource.Util.QRCodeGenerator.generatePlaceholderQR(qrData, 200)
                binding.ivQr.setImageBitmap(qrBitmap)
            } catch (e: Exception) {
                android.util.Log.e("TicketQrAdapter", "Error generating QR: ${e.message}")
                // Fallback to the simple placeholder
                val placeholderBitmap = createTextQRPlaceholder(qrData)
                binding.ivQr.setImageBitmap(placeholderBitmap)
            }
        }

        private fun createTextQRPlaceholder(qrData: String): android.graphics.Bitmap {
            val size = 200
            val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(bitmap)

            // Fill with white background
            canvas.drawColor(android.graphics.Color.WHITE)

            // Draw border
            val borderPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.BLACK
                strokeWidth = 4f
                style = android.graphics.Paint.Style.STROKE
            }
            canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), borderPaint)

            // Draw QR pattern (simple grid)
            val gridPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.BLACK
            }

            val gridSize = 8
            val cellSize = size / gridSize

            // Create a simple pattern based on qr_data hash
            val hash = qrData.hashCode()
            for (i in 0 until gridSize) {
                for (j in 0 until gridSize) {
                    if ((hash + i * gridSize + j) % 3 == 0) {
                        canvas.drawRect(
                            (i * cellSize).toFloat(),
                            (j * cellSize).toFloat(),
                            ((i + 1) * cellSize).toFloat(),
                            ((j + 1) * cellSize).toFloat(),
                            gridPaint
                        )
                    }
                }
            }

            // Draw "QR" text in center
            val textPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.BLUE
                textSize = 24f
                textAlign = android.graphics.Paint.Align.CENTER
                isFakeBoldText = true
            }
            canvas.drawText("QR", size/2f, size/2f + 8f, textPaint)

            return bitmap
        }

        // Helper method to decode base64 QR code
        private fun decodeBase64ToBitmap(base64String: String): android.graphics.Bitmap? {
            return try {
                // Remove data URL prefix if present
                val cleanBase64 = if (base64String.startsWith("data:")) {
                    base64String.substring(base64String.indexOf(",") + 1)
                } else {
                    base64String
                }

                // Skip the placeholder base64 from your API
                if (cleanBase64.trim() == "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==") {
                    android.util.Log.d("TicketQrAdapter", "Skipping placeholder base64")
                    return null
                }

                val decodedBytes = android.util.Base64.decode(cleanBase64, android.util.Base64.DEFAULT)
                android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            } catch (e: Exception) {
                android.util.Log.e("TicketQrAdapter", "Error decoding base64: ${e.message}")
                null
            }
        }
    }
}