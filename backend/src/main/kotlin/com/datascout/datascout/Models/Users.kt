package com.datascout.datascout.models


data class Users(
    val id: Int?,
    val username: String,
    val password: String,
    val email: String,
    val phone: String
)


data class UsersDto(
    val username: String,
    val password: String
)