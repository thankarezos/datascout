package com.datascout.datascout.dto

data class RegisterDto(
    val username: String,
    val password: String,
    val confirm: String,
    val email: String,
)
