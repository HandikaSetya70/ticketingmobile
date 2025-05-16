package com.dicoding.ticketingsystem.Main.Profile.ProfileDetails

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.ticketingsystem.DataSource.Response.WalletDetailResponse
import com.dicoding.ticketingsystem.R
import net.glxn.qrgen.android.QRCode

class WalletAdapter(
    private val wallets: List<WalletDetailResponse>,
    private val onDeleteClick: (WalletDetailResponse) -> Unit
) : RecyclerView.Adapter<WalletAdapter.WalletViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WalletViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_wallet, parent, false)
        return WalletViewHolder(view)
    }

    override fun onBindViewHolder(holder: WalletViewHolder, position: Int) {
        holder.bind(wallets[position], position)
    }

    override fun getItemCount(): Int = wallets.size

    inner class WalletViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvWalletTitle: TextView = itemView.findViewById(R.id.tv_wallet_title)
        private val tvWalletAddressValue: TextView = itemView.findViewById(R.id.tv_wallet_address_value)
        private val ivQr: ImageView = itemView.findViewById(R.id.iv_qr)
        private val btnDelete: TextView = itemView.findViewById(R.id.btn_delete)
        private val ivCopy: ImageView = itemView.findViewById(R.id.iv_copy)

        fun bind(wallet: WalletDetailResponse, position: Int) {
            // Set wallet number
            tvWalletTitle.text = "Wallet #${position + 1}"

            // Format and set wallet address
            tvWalletAddressValue.text = formatWalletAddress(wallet.wallet_address)

            // Generate QR code for wallet address
            try {
                val qrBitmap = QRCode.from(wallet.wallet_address)
                    .withSize(250, 250)
                    .bitmap()
                ivQr.setImageBitmap(qrBitmap)
            } catch (e: Exception) {
                // If QR generation fails, keep the default image
            }

            // Set up delete button click listener
            btnDelete.setOnClickListener {
                onDeleteClick(wallet)
            }

            // Set up copy button click listener
            ivCopy.setOnClickListener {
                copyToClipboard(itemView.context, wallet.wallet_address)
                Toast.makeText(itemView.context, "Address copied to clipboard", Toast.LENGTH_SHORT).show()
            }
        }

        private fun formatWalletAddress(address: String): String {
            return if (address.length > 10) {
                val prefix = address.take(6)
                val suffix = address.takeLast(4)
                "$prefix...$suffix"
            } else {
                address
            }
        }

        private fun copyToClipboard(context: Context, text: String) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Wallet Address", text)
            clipboard.setPrimaryClip(clip)
        }
    }
}