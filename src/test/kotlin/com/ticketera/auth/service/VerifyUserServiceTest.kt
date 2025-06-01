package com.ticketera.auth.service

import com.ticketera.auth.model.VerifyUser
import com.ticketera.auth.props.EmailMessage
import com.ticketera.auth.repository.VerifyUserRepository
import io.mockk.every

import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import software.amazon.awssdk.services.ses.model.SendEmailResponse


import java.time.Instant
import java.util.UUID


class VerifyUserServiceTest {
    private val emailService: EmailService = mockk()
    private val verifyUserRepository: VerifyUserRepository = mockk()

    private val emailMessage = EmailMessage(
        true,
        "http://localhost:3445/verify",
        "someone@opustix.com",
        "verification email",
        "message0",
        "message1", "message2"
    )

    private val service = VerifyUserService(verifyUserRepository, emailService, emailMessage)

    @Test
    fun shouldSendAnEmailToVerifyTheUser() {
        every { verifyUserRepository.save(any()) } returns VerifyUser(
            UUID.randomUUID(),
            "useremail@mail.com",
            Instant.now().toEpochMilli()
        )

        every { emailService.send(any(), any(), any(), any()) } returns Result.success(
            SendEmailResponse.builder().build()
        )

        service.sendVerificationEmail("email0@to.com")

        verify { verifyUserRepository.save(any()) }
        verify { emailService.send(any(), any(), any(), any()) }
    }
}