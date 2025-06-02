package com.ticketera.auth.repository

import com.ticketera.auth.model.VerifyUser
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface VerifyUserRepository : JpaRepository<VerifyUser, UUID> {
    fun findByEmail(email: String): VerifyUser?
}