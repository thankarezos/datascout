package com.datascout.datascout.repositories

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import com.datascout.datascout.models.Users
import java.util.UUID

@Repository
class UserRepository(private val jdbcTemplate: JdbcTemplate) {

    // This is to be replaced with actual database access
    private val users: MutableList<Users> = mutableListOf(
        Users(id = 1, username = "user1", password = "pass1", email = "user1@example.com", phone = "1234567890", token = "token1"),
        Users(id = 2, username = "user2", password = "pass2", email = "user2@example.com", phone = "9876543210", token = "token2")
    )

    fun findUsernameByID(id: Int): String? {
        val user = users.find { it.id == id }
        return user?.username
    }

    fun findPasswordByID(id: Int): String? {
        val user = users.find { it.id == id }
        return user?.password
    }

    fun isLoginValid(usernameToCheck: String?, passwordToCheck: String?, usernameToLogin: String, passwordToLogin: String): Boolean {
        return usernameToCheck == usernameToLogin && passwordToCheck == passwordToLogin
    }

    // This is not correct yet
    fun save(user: Users): Users {
        users.add(user)
        return user
    }

    fun registerUser(id: Int, username: String, password: String, email: String, phone: String): Boolean {
        
        var token: String
        do {
            token = UUID.randomUUID().toString()
        } while (tokenExists(token))


        if (users.any { it.username == username }) {
            return false // Username already exists
        }

        val newUser = Users(id, username, password, email, phone, token)
        save(newUser)

        return true
    }

    private fun tokenExists(token: String): Boolean {
        val sql = "SELECT COUNT(*) FROM Users WHERE token = ?"
        return jdbcTemplate.queryForObject(sql, Int::class.java, token) ?: 0 > 0
    }

}
