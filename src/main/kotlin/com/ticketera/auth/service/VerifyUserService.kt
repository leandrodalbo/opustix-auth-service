package com.ticketera.auth.service

import com.ticketera.auth.model.VerifyUser
import com.ticketera.auth.props.EmailMessage
import com.ticketera.auth.repository.VerifyUserRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class VerifyUserService(
    private val verifyUserRepository: VerifyUserRepository,
    private val sendEmailService: EmailService,
    private val message: EmailMessage
) {

    fun sendVerificationEmail(email: String) {

        val expiry = Instant.now().plus(24, ChronoUnit.HOURS)

        val saved = verifyUserRepository.save(VerifyUser(null, email, expiry.toEpochMilli()))

        sendEmailService.send(
            message.from, saved.email, message.subject,
            "${message.message0}\n${message.link}?token=${saved.token}"
        )
    }

}