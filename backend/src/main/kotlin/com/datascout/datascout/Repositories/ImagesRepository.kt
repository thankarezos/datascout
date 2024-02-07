
package com.datascout.datascout.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import com.datascout.datascout.models.Image



@Repository
interface ImageRepository : JpaRepository<Image, Long> {
    fun findAllByUserId(userId: Long): List<Image>
}
