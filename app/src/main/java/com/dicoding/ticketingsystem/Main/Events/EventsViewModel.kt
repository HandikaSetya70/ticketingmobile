package com.dicoding.ticketingsystem.Main.Events

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.ticketingsystem.DataSource.Repository.EventsRepository
import com.dicoding.ticketingsystem.DataSource.Response.Event
import kotlinx.coroutines.launch

class EventsViewModel(application: Application) : AndroidViewModel(application) {
    private val eventsRepository = EventsRepository(application.applicationContext)

    private val _events = MutableLiveData<Result<List<Event>>>()
    val events: LiveData<Result<List<Event>>> = _events

    fun loadEvents(
        upcoming: Boolean? = true, // Default to upcoming events
        past: Boolean? = null,
        sort: String = "event_date",
        order: String = "asc"
    ) {
        viewModelScope.launch {
            try {
                _events.value = Result.Loading
                val result = eventsRepository.getEvents(
                    upcoming = upcoming,
                    past = past,
                    sort = sort,
                    order = order
                )
                _events.value = when {
                    result.isSuccess -> Result.Success(result.getOrNull()!!)
                    else -> Result.Error(result.exceptionOrNull()?.message ?: "Unknown error")
                }
            } catch (e: Exception) {
                _events.value = Result.Error(e.message ?: "Unknown error")
            }
        }
    }
}

sealed class Result<out T> {
    object Loading : Result<Nothing>()
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
}