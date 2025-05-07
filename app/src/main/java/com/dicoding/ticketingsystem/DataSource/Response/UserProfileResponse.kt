package com.dicoding.ticketingsystem.DataSource.Response

data class UserProfileResponse(
    val status: String,
    val message: String,
    val data: UserProfileData
)

data class UserProfileData(
    val user_id: String,
    val auth_id: String,
    val id_number: String,
    val id_name: String,
    val dob: String,
    val id_picture_url: String,
    val verification_status: String,
    val role: String,
    val created_at: String,
    val updated_at: String
)