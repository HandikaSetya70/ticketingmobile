package com.dicoding.ticketingsystem.data.response

data class LoginResponse(
    val user: User,
    val session: Session,
    val profile: Profile? // Made nullable to handle cases where profile might be null
)

data class User(
    val id: String,
    val email: String,
    val created_at: String
)

data class Session(
    val access_token: String,
    val refresh_token: String,
    val expires_in: Int
)

data class Profile(
    val user_id: String,
    val id_name: String?,
    val verification_status: String?,
    val role: String?
)