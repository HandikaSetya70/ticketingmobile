package com.dicoding.ticketingsystem

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dicoding.ticketingsystem.data.SessionManager
import com.dicoding.ticketingsystem.databinding.ActivityStarterBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * StarterActivity acts as the entry point of the application
 * It shows a welcome screen with login and register options
 */
class StarterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStarterBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStarterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize session manager
        sessionManager = SessionManager(this)

        // Check login status
        checkLoginStatus()

        // Setup click listeners
        setupClickListeners()

        // Play animations
        playAnimation()
    }

    private fun checkLoginStatus() {
        lifecycleScope.launch {
            // Check if user is already logged in
            val isLoggedIn = sessionManager.isLoggedIn.first()

            if (isLoggedIn) {
                // User is logged in, go to main activity
                startActivity(Intent(this@StarterActivity, MainActivity::class.java))
                finish()
            }
        }
    }

    private fun setupClickListeners() {
        // Login button click
        binding.btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        // Register button click
        binding.btnRegister.setOnClickListener {
            // For now, do nothing or show a message as RegisterActivity is not implemented yet
            // startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun playAnimation() {
        // Animate title
        val titleAnimator = ObjectAnimator.ofFloat(binding.titleTextView, View.ALPHA, 1f).setDuration(500)

        // Animate description
        val descAnimator = ObjectAnimator.ofFloat(binding.descTextView, View.ALPHA, 1f).setDuration(500)

        // Animate login button
        val loginButtonAnimator = ObjectAnimator.ofFloat(binding.btnLogin, View.ALPHA, 1f).setDuration(500)

        // Animate register button
        val registerButtonAnimator = ObjectAnimator.ofFloat(binding.btnRegister, View.ALPHA, 1f).setDuration(500)

        // Play animations in sequence
        AnimatorSet().apply {
            playSequentially(
                titleAnimator,
                descAnimator,
                AnimatorSet().apply {
                    playTogether(loginButtonAnimator, registerButtonAnimator)
                }
            )
            startDelay = 500
            start()
        }
    }
}