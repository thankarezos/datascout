import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.stereotype.Service
import com.datascout.datascout.Repositories.UserRepository

@RestController
@RequestMapping("/api")
class UserController(private val userRepository: UserRepository) {

    @PostMapping("/login")
    fun login(@RequestBody loginRequest: LoginRequest): ResponseEntity<String> {

        val usernameToLogin = loginRequest.username
        val passwordToLogin = loginRequest.password

        var ID = 1 //temp
        val usernameToCheck = userRepository.findUsernameByID(ID)?
        val passwordToCheck = userRepository.findPasswordByID(ID)?

        return if (userRepository.isLoginValid(usernameToCheck, passwordToCheck, usernameToLogin, passwordToLogin)) {
            ResponseEntity.ok("Login successful")
        } else {
            ResponseEntity.status(401).body("Invalid username or password")
        }
    }    

    @PostMapping("/register")
    fun register(@RequestBody registrationRequest: RegistrationRequest): ResponseEntity<String> {

        val ID = 1 //temp
        val username = registrationRequest.username
        val password = registrationRequest.password

        return if (userRepository.registerUser(ID, username, password)) {
            ResponseEntity.ok("Registration successful")
        } else {
            ResponseEntity.status(400).body("Username already exists")
        }
    }

    data class LoginRequest(val username: String, val password: String)
    data class RegistrationRequest(val username: String, val password: String)
}
