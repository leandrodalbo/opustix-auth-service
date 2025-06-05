package com.ticketera.auth.repository

import com.ticketera.auth.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface UserRepository : JpaRepository<User, UUID> {
    fun existsByEmail(email: String): Boolean
    fun findByEmail(email: String): User?

    @Query("SELECT u FROM User u JOIN u.refreshTokens rt WHERE rt.token = :token")
    fun findByRefreshToken(@Param("token") token: UUID): User?
}