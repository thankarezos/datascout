package com.datascout.datascout.service

import com.datascout.datascout.Repositories.UserRepository
import com.datascout.datascout.models.Users
import org.springframework.stereotype.Service
import java.util.*


@Service
class UserService(val userRepo: UserRepository) {

    // This is to be replaced with actual database access
    private val users: MutableList<Users> = mutableListOf(
        Users(id = 1, username = "user1", password = "pass1", email = "user1@example.com", token = "token1"),
        Users(id = 2, username = "user2", password = "pass2", email = "user2@example.com", token = "token2")
    )

    fun findUsernameByID(id: Int): String? {
        val user = users.find { it.id == id }
        return user?.username
    }

    fun findPasswordByID(id: Int): String? {
        val user = users.find { it.id == id }
        return user?.password
    }

    fun findUserByUserNameAndPassword(username: String, password: String): Users? {
        return userRepo.findByUsernameAndPassword(username, password)
    }

    fun isLoginValid(usernameToCheck: String?, passwordToCheck: String?, usernameToLogin: String, passwordToLogin: String): Boolean {
        return usernameToCheck == usernameToLogin && passwordToCheck == passwordToLogin
    }

    // This is not correct yet

    fun registerUser(username: String, password: String, email: String): Boolean {


        val token = UUID.randomUUID().toString()

        if(userRepo.findByUsername(username) != null) {
            return false
        }
        val newUser = Users(
            username = username,
            password = password,
            email = email,
            token = token
        )


        userRepo.save(newUser)

        return true
    }

//    private fun tokenExists(token: String): Boolean {
//        val sql = "SELECT COUNT(*) FROM Users WHERE token = ?"
//        return jdbcTemplate.queryForObject(sql, Int::class.java, token) ?: 0 > 0
//    }

}
