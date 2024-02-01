package com.datascout.datascout.models

import jakarta.persistence.*


@Entity
@Table(name = "users")
data class Users(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,
    val username: String = "",
    val password: String = "",
    val email: String = "",
    val token: String = ""
)