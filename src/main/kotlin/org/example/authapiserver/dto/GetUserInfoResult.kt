package org.example.authapiserver.dto

import software.amazon.awssdk.services.cognitoidentityprovider.model.UserType
import java.util.UUID

data class GetUserInfoResult(
    val userId: UUID,
    val email: String?,
    val emailVerified: Boolean?,
    val phoneNumber: String?,
    val phoneNumberVerified: Boolean?,
    val name: String?,
    val userStatus: String?,
    val enabled: Boolean?,
    val createdAt: String?,
    val updatedAt: String?,
) {
    companion object {
        fun of(userId: UUID, user: UserType): GetUserInfoResult {
            val attributes = user.attributes().associate { it.name() to it.value() }
            return GetUserInfoResult(
                userId = userId,
                email = attributes["email"],
                emailVerified = attributes["email_verified"]?.toBoolean(),
                phoneNumber = attributes["phone_number"],
                phoneNumberVerified = attributes["phone_number_verified"]?.toBoolean(),
                name = attributes["name"],
                userStatus = user.userStatusAsString(),
                enabled = user.enabled(),
                createdAt = user.userCreateDate()?.toString(),
                updatedAt = user.userLastModifiedDate()?.toString()
            )
        }
    }
}
