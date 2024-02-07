package com.datascout.datascout.service

import com.datascout.datascout.Repositories.UserRepository
import com.datascout.datascout.models.Users
import org.springframework.stereotype.Service


@Service
class UserService(val userRepo: UserRepository) {


    fun findUserByUserNameAndPassword(username: String, password: String): Users? {
        return userRepo.findByUsernameAndPassword(username, password)
    }

    fun registerUser(username: String, password: String, email: String): Boolean {

        if(userRepo.findByUsername(username) != null) {
            return false
        }
        val newUser = Users(
            username = username,
            password = password,
            email = email,
        )


        userRepo.save(newUser)

        return true
    }

}
