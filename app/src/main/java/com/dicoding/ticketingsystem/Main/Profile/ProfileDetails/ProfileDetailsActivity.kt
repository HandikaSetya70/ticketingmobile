package com.dicoding.ticketingsystem.Main.Profile.ProfileDetails

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.dicoding.ticketingsystem.R
import com.dicoding.ticketingsystem.data.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ProfileDetailsActivity : AppCompatActivity() {

    private lateinit var viewModel: ProfileDetailsViewModel
    private lateinit var sessionManager: SessionManager

    private lateinit var btnClose: ImageView
    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvIdNumber: TextView
    private lateinit var tvDob: TextView
    private lateinit var tvVerificationStatus: TextView
    private lateinit var ivId: ImageView
    private lateinit var loadingOverlay: ConstraintLayout
    private lateinit var llNonUser: LinearLayout
    private lateinit var btnRegister: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile_details)

        // Initialize SessionManager
        sessionManager = SessionManager(this)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[ProfileDetailsViewModel::class.java]

        // Initialize views
        initViews()

        // Set click listeners
        setClickListeners()

        // Check if user is logged in
        checkLoginStatus()

        // Observe data changes
        observeViewModel()
    }

    private fun initViews() {
        btnClose = findViewById(R.id.btn_close)
        tvName = findViewById(R.id.tv_name)
        tvEmail = findViewById(R.id.tv_email)
        tvIdNumber = findViewById(R.id.tv_id_number)
        tvDob = findViewById(R.id.tv_dob)
        tvVerificationStatus = findViewById(R.id.tv_verification_status)
        ivId = findViewById(R.id.iv_id)
        loadingOverlay = findViewById(R.id.loading_overlay)
        llNonUser = findViewById(R.id.ll_nonuser)
        btnRegister = findViewById(R.id.btn_register)
    }

    private fun setClickListeners() {
        btnClose.setOnClickListener {
            finish()
        }

        btnRegister.setOnClickListener {
            // Navigate to Registration/Login screen
            // TODO: Implement navigation to login screen
        }
    }

    private fun checkLoginStatus() {
        CoroutineScope(Dispatchers.Main).launch {
            val isLoggedIn = sessionManager.isLoggedIn.first()
            if (isLoggedIn) {
                // User is logged in, load profile data
                loadingOverlay.visibility = View.VISIBLE
                viewModel.getUserProfile(this@ProfileDetailsActivity)
            } else {
                // User is not logged in, show register now layout
                llNonUser.visibility = View.VISIBLE
            }
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(this) { errorMessage ->
            Log.e("ProfileDetailsActivity", "Error: $errorMessage")
            // You can display an error message to the user here
        }

        viewModel.userProfile.observe(this) { userProfile ->
            // Set ID picture with Glide
            Glide.with(this)
                .load(userProfile.id_picture_url)
                .placeholder(R.drawable.id)
                .error(R.drawable.id)
                .into(ivId)

            // Set text fields
            tvName.text = userProfile.id_name
            tvIdNumber.text = userProfile.id_number
            tvDob.text = viewModel.formatDate(userProfile.dob)

            // Set verification status with proper formatting and color
            val verificationStatus = userProfile.verification_status.capitalize()
            tvVerificationStatus.text = verificationStatus

            // Set color based on verification status
            val colorRes = if (userProfile.verification_status.lowercase() == "approved") {
                R.color.green
            } else {
                R.color.red
            }
            tvVerificationStatus.setBackgroundTintList(resources.getColorStateList(colorRes, null))
        }

        viewModel.email.observe(this) { email ->
            tvEmail.text = email
        }
    }
}

// Extension function to capitalize the first letter
private fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}