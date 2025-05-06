package com.dicoding.ticketingsystem.Main.MyTickets.Tickets

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dicoding.ticketingsystem.DataSource.Repository.TicketsRepository
import com.dicoding.ticketingsystem.DataSource.Response.UserTicketsResponse
import com.dicoding.ticketingsystem.data.SessionManager
import kotlinx.coroutines.launch

class TicketsViewModel(application: Application) : AndroidViewModel(application) {
    // TicketsRepository no longer needs SessionManager directly
    private val ticketsRepository = TicketsRepository(application.applicationContext)

    private val _tickets = MutableLiveData<Result<UserTicketsResponse>>()
    val tickets: LiveData<Result<UserTicketsResponse>> = _tickets

    fun loadUserTickets(
        userId: String? = null,
        status: String? = null,
        eventId: String? = null,
        groupByEvent: Boolean = false
    ) {
        viewModelScope.launch {
            try {
                _tickets.value = Result.Loading
                val result = ticketsRepository.getUserTickets(
                    userId = userId,
                    status = status,
                    eventId = eventId,
                    groupByEvent = groupByEvent
                )
                _tickets.value = when {
                    result.isSuccess -> Result.Success(result.getOrNull()!!)
                    else -> Result.Error(result.exceptionOrNull()?.message ?: "Unknown error")
                }
            } catch (e: Exception) {
                _tickets.value = Result.Error(e.message ?: "Unknown error")
            }
        }
    }
}

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
    object Loading : Result<Nothing>()
}