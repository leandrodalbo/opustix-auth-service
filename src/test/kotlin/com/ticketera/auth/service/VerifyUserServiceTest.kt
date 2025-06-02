package com.ticketera.auth.service

import com.ticketera.auth.errors.AuthException
import com.ticketera.auth.model.VerifyEmailMessageKey
import com.ticketera.auth.model.VerifyUser
import com.ticketera.auth.props.VerifyEmailMessage
import com.ticketera.auth.repository.VerifyUserRepository
import io.mockk.every

import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Test
import software.amazon.awssdk.services.ses.model.SendEmailResponse

import java.time.Instant
import java.util.Optional
import java.util.UUID

class VerifyUserServiceTest {
    private val emailService: EmailService = mockk()
    private val verifyUserRepository: VerifyUserRepository = mockk()

    private val emailMessage = VerifyEmailMessage(
        true,
        "http://localhost:3445/verify/user",
        "someone@opustix.com",
        "s0", "m0", "s1", "m1",
        "s2", "m2", "s3", "m3",
        "statement"
    )


    @Test
    fun shouldSendAnEmailToVerifyTheUser() {
        val service = VerifyUserService(verifyUserRepository, emailService, emailMessage)

        every { verifyUserRepository.findByEmail(any()) } returns null
        every { verifyUserRepository.save(any()) } returns VerifyUser(
            UUID.randomUUID(),
            "useremail@mail.com",
            Instant.now().toEpochMilli()
        )
        every { emailService.send(any(), any(), any(), any()) } returns Result.success(
            SendEmailResponse.builder().build()
        )

        service.sendVerificationEmail("email0@to.com", VerifyEmailMessageKey.VERIFY_EMAIL)

        verify { verifyUserRepository.findByEmail(any()) }
        verify { verifyUserRepository.save(any()) }
        verify { emailService.send(any(), any(), any(), any()) }
    }

    @Test
    fun shouldSendAnEmailAndUpdateTheToken() {
        val service = VerifyUserService(verifyUserRepository, emailService, emailMessage)

        every { verifyUserRepository.findByEmail(any()) } returns VerifyUser(
            UUID.randomUUID(),
            "email@to.com",
            Instant.now().toEpochMilli()
        )
        every { verifyUserRepository.save(any()) } returns VerifyUser(
            UUID.randomUUID(),
            "useremail@mail.com",
            Instant.now().toEpochMilli()
        )
        every { emailService.send(any(), any(), any(), any()) } returns Result.success(
            SendEmailResponse.builder().build()
        )
        service.sendVerificationEmail("email0@to.com", VerifyEmailMessageKey.VERIFY_EMAIL)

        verify { verifyUserRepository.findByEmail(any()) }
        verify { verifyUserRepository.save(any()) }
        verify { emailService.send(any(), any(), any(), any()) }
    }

    @Test
    fun shouldNotifyASuccessfulVerification() {
        val service = VerifyUserService(verifyUserRepository, emailService, emailMessage)

        every { verifyUserRepository.findByEmail(any()) } returns VerifyUser(
            UUID.randomUUID(),
            "email@to.com",
            Instant.now().toEpochMilli()
        )
        every { verifyUserRepository.delete(any()) } returns Unit
        every { emailService.send(any(), any(), any(), any()) } returns Result.success(
            SendEmailResponse.builder().build()
        )

        service.sendVerificationEmail("email0@to.com", VerifyEmailMessageKey.SUCCESSFULLY_VERIFIED)

        verify { verifyUserRepository.findByEmail(any()) }
        verify { verifyUserRepository.delete(any()) }
        verify { emailService.send(any(), any(), any(), any()) }
    }

    @Test
    fun shouldNotSendEmailsIfNotEnabledAfterVerified() {
        val service =
            VerifyUserService(verifyUserRepository, emailService, emailMessage.copy(enabled = false))
        every { verifyUserRepository.findByEmail(any()) } returns VerifyUser(
            UUID.randomUUID(),
            "email@to.com",
            Instant.now().toEpochMilli()
        )

        every { verifyUserRepository.delete(any()) } returns Unit

        service.sendVerificationEmail("email0@to.com", VerifyEmailMessageKey.SUCCESSFULLY_VERIFIED)

        verify { verifyUserRepository.findByEmail(any()) }
        verify { verifyUserRepository.delete(any()) }
    }

    @Test
    fun shouldNotSendAnEmailAfterSavingWhenNotEnabled() {
        val service =
            VerifyUserService(verifyUserRepository, emailService, emailMessage.copy(enabled = false))

        every { verifyUserRepository.findByEmail(any()) } returns VerifyUser(
            UUID.randomUUID(),
            "email@to.com",
            Instant.now().toEpochMilli()
        )
        every { verifyUserRepository.save(any()) } returns VerifyUser(
            UUID.randomUUID(),
            "useremail@mail.com",
            Instant.now().toEpochMilli()
        )

        service.sendVerificationEmail("email0@to.com", VerifyEmailMessageKey.VERIFY_EMAIL)

        verify { verifyUserRepository.findByEmail(any()) }
        verify { verifyUserRepository.save(any()) }
    }

    @Test
    fun shouldFindVerifyUserFromToken() {
        val service =
            VerifyUserService(verifyUserRepository, emailService, emailMessage.copy(enabled = false))
        val token = UUID.randomUUID()
        every { verifyUserRepository.findById(any()) } returns Optional.of(
            VerifyUser(
                token,
                "email@to.com",
                Instant.now().toEpochMilli()
            )
        )


        assertThat(service.findFromToken(token.toString()).token).isEqualTo(token)

        verify { verifyUserRepository.findById(any()) }
    }

    @Test
    fun shouldThrowExceptionWhenNotFoundUsingToken() {
        val service =
            VerifyUserService(verifyUserRepository, emailService, emailMessage.copy(enabled = false))
        val token = UUID.randomUUID()
        every { verifyUserRepository.findById(any()) } returns Optional.empty()


        assertThatExceptionOfType(AuthException::class.java)
            .isThrownBy {
                service.findFromToken(token.toString())
            }

        verify { verifyUserRepository.findById(any()) }
    }
}