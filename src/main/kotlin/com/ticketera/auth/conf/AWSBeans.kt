package com.ticketera.auth.conf

import com.ticketera.auth.props.AWSProps
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.ses.SesClient

@Configuration
class AWSBeans(private val awsProps: AWSProps) {

    @Bean
    fun sesClient(): SesClient = SesClient.builder()
        .region(Region.of(awsProps.region))
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(
                    awsProps.key,
                    awsProps.secret
                )
            )
        ).build()
}