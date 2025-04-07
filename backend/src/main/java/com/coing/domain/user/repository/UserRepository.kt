package com.coing.domain.user.repository

import com.coing.domain.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, UUID> {

    fun findByEmail(email: String): Optional<User>

    @Modifying
    @Query("DELETE FROM User u WHERE u.verified = false AND u.createdAt < :threshold")
    fun deleteUnverifiedUsers(@Param("threshold") threshold: LocalDateTime): Int
}
