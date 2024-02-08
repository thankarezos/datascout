package com.datascout.datascout.controllers

import com.datascout.datascout.JwtUtil
import com.datascout.datascout.dto.RegisterDto
import com.datascout.datascout.dto.Response
import com.datascout.datascout.dto.UsersDto
import com.datascout.datascout.service.UserService
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class tokenResponse(
    val token: String,
    val userId: Int
)

@RestController
@RequestMapping("/api")
class UserController(private val userService: UserService, private val jwtUtil: JwtUtil) {

    @PostMapping("/login")
    fun login(@RequestBody loginRequest: UsersDto,
              response: HttpServletResponse
    ): ResponseEntity<Response<tokenResponse>> {

        val usernameToLogin = loginRequest.username
        val passwordToLogin = loginRequest.password

        val user = userService.findUserByUserNameAndPassword(usernameToLogin, passwordToLogin)

        if (user == null) {
            return ResponseEntity.status(401).body(Response("Invalid username or password"))
        }
        else {
            val token = jwtUtil.generateToken(user.id.toString())
            val userId = user.id
            val cookie = Cookie("jwt", token)
            cookie.path = "/"
            cookie.isHttpOnly = true
            response.addCookie(cookie)
            return ResponseEntity.ok(Response( tokenResponse(token, userId)))
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
