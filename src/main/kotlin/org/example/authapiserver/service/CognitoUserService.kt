package org.example.authapiserver.service

import io.github.oshai.kotlinlogging.KotlinLogging
import org.example.authapiserver.dto.GetUserInfoResult
import org.example.authapiserver.exception.NotFoundUserException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUsersRequest
import java.util.UUID

private val logger = KotlinLogging.logger {}

@Service
class CognitoUserService(
    private val cognitoClient: CognitoIdentityProviderClient,
    @Value("\${cognito.userPoolId}") private val userPoolId: String
) {
    fun getUserInfo(userId: UUID): GetUserInfoResult {
        logger.info { "Getting user info from Cognito: $userId" }

        val request = ListUsersRequest.builder()
            .userPoolId(userPoolId)
            .filter("sub = \"$userId\"")
            .limit(1)
            .build()

        val response = cognitoClient.listUsers(request)

        if (response.users().isEmpty()) {
            logger.warn { "User not found in Cognito: $userId" }
            throw NotFoundUserException(logics = "CognitoUserService.getUserInfo: listUsers returned empty for userId=$userId")
        }

        val user = response.users().first()
        logger.info { "Successfully retrieved user info: $userId" }

        return GetUserInfoResult.of(userId, user)
    }
}
