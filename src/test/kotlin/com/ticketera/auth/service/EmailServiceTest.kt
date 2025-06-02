package com.ticketera.auth.service


import com.ticketera.auth.errors.AuthException
import com.ticketera.auth.errors.Message
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Test

import software.amazon.awssdk.services.ses.SesClient
import software.amazon.awssdk.services.ses.model.SendEmailRequest
import software.amazon.awssdk.services.ses.model.SendEmailResponse


class EmailServiceTest {

    private val sesClient: SesClient = mockk()
    private val emailService = EmailService(sesClient)

    @Test
    fun shouldSendAnEmailToVerifyTheUser() {
        every { sesClient.sendEmail(ofType(SendEmailRequest::class)) } returns SendEmailResponse.builder().build()

        emailService.send("email0@from.com", "email0@to.com", "very your email", "please click on the link...")

        verify { sesClient.sendEmail(ofType(SendEmailRequest::class)) }
    }

    @Test
    fun shouldHandleEmailClientFailures() {
        every { sesClient.sendEmail(ofType(SendEmailRequest::class)) } throws Exception()

        assertThatExceptionOfType(AuthException::class.java).isThrownBy {
            emailService.send("email0@from.com", "email0@to.com", "very your email", "please click on the link...")
        }.withMessage(Message.EMAIL_SERVICE_FAILED.text)

        verify { sesClient.sendEmail(ofType(SendEmailRequest::class)) }
    }
}