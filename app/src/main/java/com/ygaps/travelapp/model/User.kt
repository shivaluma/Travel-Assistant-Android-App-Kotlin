package com.ygaps.travelapp

data class User(
    val id: Int, val full_name: String, val email: String, val phone: String, val address: String,
    val dob: String, val gender: Int, val email_verified: Boolean, val phone_verified: Boolean
)

