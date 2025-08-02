package org.example.authapiserver.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient

@Configuration
class AwsConfig(
    @Value("\${cognito.region}") private val region: String
) {
    @Bean
    fun cognitoIdentityProviderClient(): CognitoIdentityProviderClient {
        return CognitoIdentityProviderClient.builder()
            .region(Region.of(region))
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build()
    }
}
