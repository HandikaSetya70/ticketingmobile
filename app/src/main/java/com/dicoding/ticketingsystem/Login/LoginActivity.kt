package com.dicoding.ticketingsystem

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dicoding.ticketingsystem.data.SessionManager
import com.dicoding.ticketingsystem.databinding.ActivityLoginBinding
import com.dicoding.ticketingsystem.viewmodel.LoginViewModel
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)

        // Initialize SessionManager
        sessionManager = SessionManager(this)

        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        // Login button click
        binding.btnLogin.setOnClickListener {
            val email = binding.edLoginEmail.text.toString().trim()
            val password = binding.edLoginPass.text.toString().trim()

            // Validate inputs
            if (email.isEmpty()) {
                binding.layoutLoginEmail.error = "Email cannot be empty"
                return@setOnClickListener
            } else {
                binding.layoutLoginEmail.error = null
            }

            if (password.isEmpty()) {
                binding.layoutLoginPass.error = "Password cannot be empty"
                return@setOnClickListener
            } else {
                binding.layoutLoginPass.error = null
            }

            // Show loading state
            showLoading(true)

            // Attempt login
            viewModel.login(email, password)
        }
    }

    private fun observeViewModel() {
        viewModel.loginResult.observe(this) { result ->
            // Hide loading
            showLoading(false)

            when (result) {
                is LoginViewModel.LoginResult.Success -> {
                    // Save user session using coroutines
                    lifecycleScope.launch {
                        // Create login session with data from API response
                        sessionManager.createLoginSession(
                            userId = result.data.user.id,
                            email = result.data.user.email,
                            accessToken = result.data.session.access_token,
                            refreshToken = result.data.session.refresh_token,
                            createdAt = result.data.user.created_at,
                            expiresIn = result.data.session.expires_in,
                            idName = result.data.profile?.id_name, // Added null safety
                            verificationStatus = result.data.profile?.verification_status, // Added null safety
                            role = result.data.profile?.role // Added null safety
                        )

                        // Navigate to main activity
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    }
                }

                is LoginViewModel.LoginResult.Error -> {
                    // Show error message
                    Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                }

                null -> {
                    // Initial state, do nothing
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.loadingOverlay.visibility = View.VISIBLE
            binding.progressBar.visibility = View.VISIBLE
            binding.btnLogin.isEnabled = false
        } else {
            binding.loadingOverlay.visibility = View.GONE
            binding.progressBar.visibility = View.GONE
            binding.btnLogin.isEnabled = true
        }
    }

    override fun onResume() {
        super.onResume()
        // Reset the login result when returning to this screen
        viewModel.resetLoginResult()
    }
}