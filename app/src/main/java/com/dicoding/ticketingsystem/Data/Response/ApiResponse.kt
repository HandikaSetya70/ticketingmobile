
// ApiResponse.kt
package com.dicoding.ticketingsystem.data.response

data class ApiResponse<T>(
    val status: String,
    val message: String,
    val data: T
)