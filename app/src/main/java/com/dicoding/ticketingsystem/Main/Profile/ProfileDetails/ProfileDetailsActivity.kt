package com.dicoding.ticketingsystem.Main.Profile.ProfileDetails

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.dicoding.ticketingsystem.R
import com.dicoding.ticketingsystem.data.SessionManager
import com.dicoding.ticketingsystem.databinding.ActivityProfileDetailsBinding
import com.dicoding.ticketingsystem.databinding.DialogWalletConnectBinding
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.RequiresApi
import com.dicoding.ticketingsystem.DataSource.Response.WalletDetailResponse

class ProfileDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileDetailsBinding
    private lateinit var viewModel: ProfileDetailsViewModel
    private lateinit var sessionManager: SessionManager
    private lateinit var walletConnectDialog: Dialog

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize ViewBinding
        binding = ActivityProfileDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize SessionManager
        sessionManager = SessionManager(this)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[ProfileDetailsViewModel::class.java]

        // Set click listeners
        setClickListeners()

        // Set up RecyclerView
        binding.rvWallets.layoutManager = LinearLayoutManager(this)

        // Check if user is logged in
        checkLoginStatus()

        // Observe data changes
        observeViewModel()
    }

    private fun setClickListeners() {
        binding.btnClose.setOnClickListener {
            finish()
        }

        binding.btnRegister.setOnClickListener {
            // Navigate to Registration/Login screen
            // TODO: Implement navigation to login screen
        }

        binding.btnLink.setOnClickListener {
            showWalletConnectDialog()
        }
    }

    private fun checkLoginStatus() {
        CoroutineScope(Dispatchers.Main).launch {
            val isLoggedIn = sessionManager.isLoggedIn.first()
            if (isLoggedIn) {
                // User is logged in, load profile data
                binding.loadingOverlay.visibility = View.VISIBLE
                viewModel.getUserProfile(this@ProfileDetailsActivity)
            } else {
                // User is not logged in, show register now layout
                binding.llNonuser.visibility = View.VISIBLE
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.detailLayoutSv.visibility = if (isLoading) View.GONE else View.VISIBLE
        }

        viewModel.error.observe(this) { errorMessage ->
            Log.e("ProfileDetailsActivity", "Error: $errorMessage")
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
        }

        viewModel.userProfile.observe(this) { userProfile ->
            // Set ID picture with Glide
            Glide.with(this)
                .load(userProfile.id_picture_url)
                .placeholder(R.drawable.id)
                .error(R.drawable.id)
                .into(binding.ivId)

            // Set text fields
            binding.tvName.text = userProfile.id_name
            binding.tvIdNumber.text = userProfile.id_number
            binding.tvDob.text = viewModel.formatDate(userProfile.dob)

            // Set verification status with proper formatting and color
            val verificationStatus = userProfile.verification_status.capitalize()
            binding.tvVerificationStatus.text = verificationStatus

            // Set color based on verification status
            val colorRes = if (userProfile.verification_status.lowercase() == "approved") {
                R.color.green
            } else {
                R.color.red
            }
            binding.tvVerificationStatus.backgroundTintList = resources.getColorStateList(colorRes, null)
        }

        viewModel.email.observe(this) { email ->
            binding.tvEmail.text = email
        }

        // Observe wallet list
        viewModel.wallets.observe(this) { wallets ->
            updateWalletStatus(wallets)

            // Set up RecyclerView adapter
            binding.rvWallets.adapter = WalletAdapter(wallets) { wallet ->
                // Handle delete button click
                showDeleteWalletConfirmation(wallet)
            }
        }

        // Observe wallet connection result
        viewModel.walletConnected.observe(this) { wallet ->
            if (wallet != null) {
                // Close dialog if it's showing
                if (::walletConnectDialog.isInitialized && walletConnectDialog.isShowing) {
                    walletConnectDialog.dismiss()
                }

                Toast.makeText(this, "Wallet connected successfully", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun updateWalletStatus(wallets: List<WalletDetailResponse>) {
        if (wallets.isEmpty()) {
            binding.tvWalletStatus.text = "Not Linked"
            binding.tvWalletStatus.backgroundTintList = resources.getColorStateList(R.color.red, null)
            binding.rvWallets.visibility = View.GONE
        } else {
            binding.tvWalletStatus.text = "Linked"
            binding.tvWalletStatus.backgroundTintList = resources.getColorStateList(R.color.green, null)
            binding.rvWallets.visibility = View.VISIBLE
        }
    }

    private fun showWalletConnectDialog() {
        val dialogBinding = DialogWalletConnectBinding.inflate(layoutInflater)
        walletConnectDialog = Dialog(this)
        walletConnectDialog.setContentView(dialogBinding.root)
        walletConnectDialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        walletConnectDialog.show()

        dialogBinding.qrCardview.visibility = View.GONE
        dialogBinding.progressBar.visibility = View.VISIBLE
        dialogBinding.tvInstructions.text = "Initializing connection..."

        //Wallet Connect implementation here
    }

    private fun generateQrCode(content: String): Bitmap {
        val size = 512
        val bits = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size)
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bmp.setPixel(x, y, if (bits.get(x, y)) Color.BLACK else Color.WHITE)
            }
        }
        return bmp
    }

    private fun showDeleteWalletConfirmation(wallet: WalletDetailResponse) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Disconnect Wallet")
        builder.setMessage("Are you sure you want to disconnect this wallet?")

        builder.setPositiveButton("Disconnect") { dialog, _ ->
            // TODO: Implement wallet disconnection API call
            // For now, just refresh the wallet list
            viewModel.getUserWallets(this)
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}

// Extension function to capitalize the first letter
private fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}