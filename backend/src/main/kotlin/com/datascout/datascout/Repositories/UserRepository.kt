package com.datascout.datascout.Repositories

import com.datascout.datascout.models.Users
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<Users, Long> {
    // Basic CRUD methods are inherited
    fun findByUsername(username: String): Users?
    fun findByUsernameAndPassword(username: String, password: String): Users?
}

