package com.ticketera.auth.service


import com.ticketera.auth.errors.AuthException
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.ses.SesClient
import software.amazon.awssdk.services.ses.model.Body
import software.amazon.awssdk.services.ses.model.Content
import software.amazon.awssdk.services.ses.model.Message
import software.amazon.awssdk.services.ses.model.Destination
import software.amazon.awssdk.services.ses.model.SendEmailRequest

@Service
class EmailService(private val emailClient: SesClient) {

    fun send(emailFrom: String, emailTo: String, subject: String, message: String) = kotlin.runCatching {
        val emailRequest = SendEmailRequest.builder()
            .source(emailFrom)
            .destination(
                Destination.builder().toAddresses(emailTo).build()
            )
            .message(
                Message.builder()
                    .subject(Content.builder().data(subject).build())
                    .body(
                        Body.builder().text(
                            Content.builder()
                                .data(message).build()
                        ).build()
                    ).build()
            ).build()

        emailClient.sendEmail(emailRequest)

    }.onFailure { throw AuthException(com.ticketera.auth.errors.Message.EMAIL_SERVICE_FAILED.text) }

}