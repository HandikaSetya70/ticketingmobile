package com.dicoding.ticketingsystem.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.ticketingsystem.data.repository.AuthRepository
import com.dicoding.ticketingsystem.data.response.LoginResponse
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    private val _loginResult = MutableLiveData<LoginResult<LoginResponse>?>()
    val loginResult: LiveData<LoginResult<LoginResponse>?> = _loginResult

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                val result = authRepository.login(email, password)
                _loginResult.value = when {
                    result.isSuccess -> LoginResult.Success(result.getOrNull()!!)
                    else -> LoginResult.Error(result.exceptionOrNull()?.message ?: "Unknown error occurred")
                }
            } catch (e: Exception) {
                _loginResult.value = LoginResult.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun resetLoginResult() {
        _loginResult.value = null
    }

    sealed class LoginResult<out T> {
        data class Success<out T>(val data: T) : LoginResult<T>()
        data class Error(val message: String) : LoginResult<Nothing>()
    }
}