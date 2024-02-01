package com.datascout.datascout.controllers

import com.datascout.datascout.dto.RegisterDto
import com.datascout.datascout.dto.UsersDto
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import com.datascout.datascout.service.UserService

@RestController
@RequestMapping("/api")
class UserController(private val userService: UserService) {

    @PostMapping("/login")
    fun login(@RequestBody loginRequest: UsersDto): ResponseEntity<String> {

        val usernameToLogin = loginRequest.username
        val passwordToLogin = loginRequest.password

        val user = userService.findUserByUserNameAndPassword(usernameToLogin, passwordToLogin)

        return if (user == null) {
            ResponseEntity.status(401).body("Invalid username or password")
        } else {
            ResponseEntity.ok(user.token)
        }
    }    

    @PostMapping("/register")
    fun register(@RequestBody registrationRequest: RegisterDto): ResponseEntity<String> {

        val username = registrationRequest.username
        val password = registrationRequest.password
        val email = registrationRequest.email

        return if (userService.registerUser(username, password, email)) {
            ResponseEntity.ok("Registration successful")
        } else {
            ResponseEntity.status(400).body("Username already exists")
        }
    }
}
