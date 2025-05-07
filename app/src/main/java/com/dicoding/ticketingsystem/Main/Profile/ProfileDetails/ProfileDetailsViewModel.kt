package com.dicoding.ticketingsystem.Main.Profile.ProfileDetails

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.ticketingsystem.DataSource.Response.UserProfileData
import com.dicoding.ticketingsystem.data.SessionManager
import com.dicoding.ticketingsystem.data.api.ApiConfig
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class ProfileDetailsViewModel : ViewModel() {

    private val _userProfile = MutableLiveData<UserProfileData>()
    val userProfile: LiveData<UserProfileData> = _userProfile

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _email = MutableLiveData<String>()
    val email: LiveData<String> = _email

    fun getUserProfile(context: Context) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val apiService = ApiConfig.getAuthenticatedApiService(context)
                val response = apiService.getUserProfile()

                if (response.status == "success") {
                    _userProfile.value = response.data

                    // Fetch email from SessionManager
                    val sessionManager = SessionManager(context)
                    _email.value = sessionManager.email.first() ?: "Email not available"
                } else {
                    _error.value = response.message
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "An error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Format date from API (YYYY-MM-DD) to readable format (1st of January 1990)
    fun formatDate(dateString: String): String {
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val outputFormat = SimpleDateFormat("d'${getDaySuffix(dateString)}' 'of' MMMM yyyy", Locale.US)
            val date = inputFormat.parse(dateString)
            return date?.let { outputFormat.format(it) } ?: dateString
        } catch (e: Exception) {
            return dateString
        }
    }

    // Get day suffix (st, nd, rd, th)
    private fun getDaySuffix(dateString: String): String {
        val day = dateString.split("-")[2].toInt()
        return when {
            day in 11..13 -> "th"
            day % 10 == 1 -> "st"
            day % 10 == 2 -> "nd"
            day % 10 == 3 -> "rd"
            else -> "th"
        }
    }
}