package com.datascout.datascout.controllers

import com.datascout.datascout.JwtUtil
import com.datascout.datascout.Repositories.UserRepository
import com.datascout.datascout.dto.RegisterDto
import com.datascout.datascout.dto.Response
import com.datascout.datascout.dto.UsersDto
import com.datascout.datascout.models.Users
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.bcrypt.BCrypt
import org.springframework.web.bind.annotation.*

data class TokenResponse(
    val token: String,
    val userId: Int
)

@RestController
@RequestMapping("/api")
class UserController(
    private val jwtUtil: JwtUtil,
    private val userRepository: UserRepository
) {

    @PostMapping("/login")
    fun login(@RequestBody loginRequest: UsersDto,
              response: HttpServletResponse
    ): ResponseEntity<Response<TokenResponse>> {

        val usernameToLogin = loginRequest.username
        val passwordToLogin = loginRequest.password

        val user = userRepository.findByUsername(usernameToLogin)



        if (user == null || !verifyPassword(passwordToLogin, user.password)) {
            return ResponseEntity.status(401).body(Response("Invalid username or password"))
        }
        else {
            val token = jwtUtil.generateToken(user.id.toString())
            val userId = user.id
            val cookie = Cookie("jwt", token)
            cookie.path = "/"
            cookie.isHttpOnly = true
//            cookie.domain = "localhost:5173"
            response.addCookie(cookie)
            return ResponseEntity.ok(Response( TokenResponse(token, userId)))
        }
    }

    @PostMapping("/logout")
    fun logout(response: HttpServletResponse): ResponseEntity<Any> {
        val cookie = Cookie("jwt", "")
        cookie.path = "/"
        cookie.isHttpOnly = true
        cookie.maxAge = 0  // Set cookie to expire immediately
        response.addCookie(cookie)
        return ResponseEntity.ok().build<Any>()
    }

    @PostMapping("/register")
    fun register(@RequestBody registrationRequest: RegisterDto): ResponseEntity<Response<String>> {

        val username = registrationRequest.username
        val password = registrationRequest.password
        val email = registrationRequest.email
        val confirm = registrationRequest.confirm

        if(password != confirm) {
            return ResponseEntity.status(401).body(Response("Passwords do not match"))
        }

        if(userRepository.findByUsername(username) != null) {
            return ResponseEntity.status(401).body(Response("Username already exists"))
        }

        if(userRepository.findByEmail(email) != null) {
            return ResponseEntity.status(401).body(Response("Email already exists"))
        }

        val newUser = Users(
            username = username,
            password = hashPassword(password),
            email = email,
        )


        userRepository.save(newUser)

        return ResponseEntity.ok(Response())

    }


    fun hashPassword(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }

    fun verifyPassword(candidate: String, hashed: String): Boolean {
        return BCrypt.checkpw(candidate, hashed)
    }


}
