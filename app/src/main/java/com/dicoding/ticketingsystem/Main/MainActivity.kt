package com.dicoding.ticketingsystem

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dicoding.ticketingsystem.data.SessionManager
import com.dicoding.ticketingsystem.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize SessionManager
        sessionManager = SessionManager(this)

        // Setup UI
//        setupUI()
//
//        // Setup click listeners
//        setupClickListeners()
    }

//    private fun setupUI() {
//        lifecycleScope.launch {
//            // Get user details from DataStore
//            val userId = sessionManager.getUserId.first()
//            val verificationStatus = sessionManager.verificationStatus.first()
//
//            // Update UI based on verification status
//            when (verificationStatus) {
//                "pending" -> {
//                    binding.tvVerificationStatus.text = "Verification Status: Pending"
//                    binding.tvVerificationMessage.text = "Your ID verification is pending approval. Some features may be limited."
//                }
//                "approved" -> {
//                    binding.tvVerificationStatus.text = "Verification Status: Approved"
//                    binding.tvVerificationMessage.text = "Your ID has been verified. You have full access to all features."
//                }
//                "rejected" -> {
//                    binding.tvVerificationStatus.text = "Verification Status: Rejected"
//                    binding.tvVerificationMessage.text = "Your ID verification was rejected. Please update your information."
//                }
//            }
//
//            // Show user ID
//            binding.tvUserId.text = "User ID: $userId"
//        }
//    }
//
//    private fun setupClickListeners() {
//        // Logout button click
//        binding.btnLogout.setOnClickListener {
//            lifecycleScope.launch {
//                // Clear user session
//                sessionManager.logoutUser()
//
//                // Show toast message
//                Toast.makeText(this@MainActivity, "Logged out successfully", Toast.LENGTH_SHORT).show()
//
//                // Navigate back to login
//                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
//                finish()
//            }
//        }
//    }
}