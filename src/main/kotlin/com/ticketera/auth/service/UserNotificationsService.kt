package com.ticketera.auth.service

import com.ticketera.auth.errors.AuthException
import com.ticketera.auth.errors.Message
import com.ticketera.auth.model.EmailMessageKey
import com.ticketera.auth.model.VerifyUser
import com.ticketera.auth.props.VerifyEmailMessage
import com.ticketera.auth.repository.VerifyUserRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.jvm.optionals.getOrElse

@Service
class UserNotificationsService(
    private val verifyUserRepository: VerifyUserRepository,
    private val sendEmailService: EmailService,
    private val emailMessage: VerifyEmailMessage

) {

    fun sendVerificationEmail(email: String, emailMessageKey: EmailMessageKey) {

        if (EmailMessageKey.SUCCESSFULLY_VERIFIED.equals(emailMessageKey)) {
            verifyUserRepository.delete(
                verifyUserRepository.findByEmail(email)
                    ?: throw AuthException(Message.NOTIFICATIONS_SERVICE_FAILED.text)
            )
            if (emailMessage.enabled) notifyUser(emailMessageKey, email, null)


        } else {
            val toBeSaved = verifyUserRepository.findByEmail(email)
                .let { it?.copy(expiry = Instant.now().plus(24, ChronoUnit.HOURS).toEpochMilli()) }
                ?: VerifyUser(
                    null,
                    email,
                    Instant.now().plus(24, ChronoUnit.HOURS).toEpochMilli()
                )
            val saved = verifyUserRepository.save(toBeSaved)
            if (emailMessage.enabled) notifyUser(emailMessageKey, saved.email, saved.token)
        }
    }

    fun sendPasswordResetEmail(emailTo: String, token: UUID?) {
        if (emailMessage.enabled) notifyUser(EmailMessageKey.PASSWORD_RESET, emailTo, token)
    }

    fun findFromToken(token: String): VerifyUser = verifyUserRepository.findById(UUID.fromString(token))
        .getOrElse { throw AuthException(Message.NOTIFICATIONS_SERVICE_FAILED.text) }

    private fun notifyUser(key: EmailMessageKey, emailTo: String, token: UUID?) {
        subjectAndMessage(key).let {
            sendEmailService.send(
                emailMessage.from,
                emailTo,
                it.subject,
                buildMessage(it.message, token).plus(emailMessage.closing)
            )
        }
    }

    private fun buildMessage(message: String, token: UUID?) = token?.let {
        "${message}\n${emailMessage.link}?token=${token}"
    } ?: "${message}\n"

    private fun subjectAndMessage(key: EmailMessageKey): SubjectAndMessage = when (key) {
        EmailMessageKey.VERIFY_EMAIL -> SubjectAndMessage(emailMessage.subject0, emailMessage.message0)
        EmailMessageKey.NOT_VERIFIED_LOGIN -> SubjectAndMessage(emailMessage.subject1, emailMessage.message1)
        EmailMessageKey.NOT_VERIFIED_SIGN_UP -> SubjectAndMessage(emailMessage.subject2, emailMessage.message2)
        EmailMessageKey.SUCCESSFULLY_VERIFIED -> SubjectAndMessage(emailMessage.subject3, emailMessage.message3)
        EmailMessageKey.PASSWORD_RESET -> SubjectAndMessage(emailMessage.subject4, emailMessage.message4)
    }

    private data class SubjectAndMessage(val subject: String, val message: String)

}