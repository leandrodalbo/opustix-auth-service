package com.ticketera.auth.model

import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.Id
import jakarta.persistence.Column
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "verify_user")
data class VerifyUser(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false, unique = true)
    val token: UUID? = null,

    @Column(nullable = false, unique = true)
    val email: String,

    @Column(nullable = false)
    val expiry: Long
) {

    fun isExpired(): Boolean = Instant.now().toEpochMilli() > expiry
}
