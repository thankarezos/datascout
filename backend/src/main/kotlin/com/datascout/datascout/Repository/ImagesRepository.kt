
package com.datascout.datascout.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import com.datascout.datascout.models.Image



@Repository
interface ImageRepository : JpaRepository<Image, Long> {
    // Basic CRUD methods are inherited
}
