import org.springframework.stereotype.Repository

@Repository
class UserRepository {

    // This is to be replaces with database access
    private val users: MutableList<User> = mutableListOf(
        User(id = 1, username = "user1", password = "pass1"),
        User(id = 2, username = "user2", password = "pass2")
    )
    data class User(val id:Int, val username:String, val password:String)


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

    //This is not correct yet
    fun save(user: User): User {
        users[user.username] = user
        return user
    }


    fun registerUser(id: Int, username: String, password: String): Boolean {

        if (users.any { it.username == username })
            return false // Username already exists

        val newUser = User(id, username, password) //this will also need to be changed when the databse comes
        save(newUser)

        return true
    }
}
