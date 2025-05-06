package com.dicoding.ticketingsystem.DataSource.Response

data class EventsResponse(
    val status: String,
    val message: String,
    val data: List<Event>
)