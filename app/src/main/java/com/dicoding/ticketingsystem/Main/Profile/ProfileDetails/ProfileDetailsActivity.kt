package com.dicoding.ticketingsystem.Main.Profile.ProfileDetails

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.dicoding.ticketingsystem.R
import com.dicoding.ticketingsystem.data.SessionManager
import com.dicoding.ticketingsystem.databinding.ActivityProfileDetailsBinding
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.RequiresApi

class ProfileDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileDetailsBinding
    private lateinit var viewModel: ProfileDetailsViewModel
    private lateinit var sessionManager: SessionManager

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
            // Open the specified URL instead of showing the wallet connect dialog
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
            intent.data = android.net.Uri.parse("http://setya.fwh.is")
            startActivity(intent)
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

    override fun onDestroy() {
        super.onDestroy()
    }
}

// Extension function to capitalize the first letter
private fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}