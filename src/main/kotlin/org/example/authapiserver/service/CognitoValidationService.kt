package org.example.authapiserver.service

import io.github.oshai.kotlinlogging.KotlinLogging
import org.example.authapiserver.enums.MSAServiceErrorCode
import org.example.authapiserver.dto.ExtractedUserFromToken
import org.example.authapiserver.dto.ExtractUserResponseFromServer
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtException
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

/**
 * AWS Cognito JWT 토큰 검증 서비스
 *
 * 이 서비스는 프론트엔드에서 전송된 Cognito ID Token을 검증하고
 * 사용자 정보(userType 포함)를 추출합니다.
 */
@Service
class CognitoValidationService(
    private val jwtDecoder: JwtDecoder,
    @Value("\${cognito.userPoolId}") private val userPoolId: String,
    @Value("\${cognito.clientId}") private val clientId: String,
    @Value("\${cognito.region}") private val region: String
) {

    /**
     * JWT 토큰을 검증하고 사용자 정보를 추출합니다.
     *
     * 검증 과정:
     * 1. JWT 서명 검증 (Cognito JWKS 공개키 사용)
     * 2. Issuer(iss) 검증
     * 3. Audience(aud) 또는 client_id 검증
     * 4. 만료 시간(exp) 검증
     * 5. 사용자 정보 추출 (email, phone_number, custom:userType 등)
     * 6. expectedUserType 검증 (프론트엔드 서비스 통제)
     *
     * @param token Bearer 토큰 (Bearer 접두사 제외)
     * @param expectedUserType 프론트엔드에서 기대하는 사용자 타입 (null이면 검증 생략)
     * @return 검증 결과 및 사용자 정보
     */
    fun validateTokenAndExtractInfluencerInfo(
        token: String,
    ): ExtractUserResponseFromServer {
        logger.debug { "Starting JWT token validation influencer token" }

        try {
            // JWT 디코딩 및 검증 (서명, iss, exp 자동 검증)
            val jwt: Jwt = jwtDecoder.decode(token)

            // 추가 검증: Issuer 확인
            val expectedIssuer = "https://cognito-idp.$region.amazonaws.com/$userPoolId"
            val actualIssuer = jwt.issuer?.toString()

            if (actualIssuer != expectedIssuer) {
                logger.warn { "Invalid issuer: expected=$expectedIssuer, actual=$actualIssuer" }
                return ExtractUserResponseFromServer.unauthorized(
                    errorCode = MSAServiceErrorCode.INVALID_ISSUER,
                    errorMessage = "Invalid token issuer",
                    logics = "Expected issuer: $expectedIssuer, Actual: $actualIssuer"
                )
            }

            // 추가 검증: Client ID 확인 (aud 또는 client_id)
            val tokenClientId = jwt.claims["client_id"] as? String ?: jwt.audience?.firstOrNull()
            if (tokenClientId != clientId) {
                logger.warn { "Invalid client_id: expected=$clientId, actual=$tokenClientId" }
                return ExtractUserResponseFromServer.unauthorized(
                    errorCode = MSAServiceErrorCode.INVALID_AUDIENCE,
                    errorMessage = "Invalid token audience",
                    logics = "Expected client_id: $clientId, Actual: $tokenClientId"
                )
            }

            // 사용자 정보 추출
            val extractedUser = extractUserFromToken(jwt)

            // 추가 검증: 프론트엔드에서 기대하는 userType과 일치하는지 확인
            if (extractedUser.userType != "INFLUENCER") {
                logger.warn {
                    "UserType mismatch: expected=INFLUENCER, " +
                            "actual=${extractedUser.userType}, userId=${extractedUser.userId}"
                }
                return ExtractUserResponseFromServer.forbidden(
                    errorCode = MSAServiceErrorCode.INVALID_USER_TYPE,
                    errorMessage = "User type does not match the expected type for this service",
                    logics = "Expected userType: INFLUENCER, Actual: ${extractedUser.userType}"
                )
            }

            logger.info { "Token validated successfully for user:" +
                    " ${extractedUser.userId}, userType: ${extractedUser.userType}" }

            return ExtractUserResponseFromServer.success(
                user = extractedUser,
                logics = "Token validation successful for userType: ${extractedUser.userType}"
            )

        } catch (e: JwtException) {
            logger.error(e) { "JWT validation failed" }
            return when {
                e.message?.contains("expired", ignoreCase = true) == true -> {
                    ExtractUserResponseFromServer.unauthorized(
                        errorCode = MSAServiceErrorCode.EXPIRED_TOKEN,
                        errorMessage = "Token has expired",
                        logics = e.message
                    )
                }
                e.message?.contains("signature", ignoreCase = true) == true -> {
                    ExtractUserResponseFromServer.unauthorized(
                        errorCode = MSAServiceErrorCode.INVALID_SIGNATURE,
                        errorMessage = "Invalid token signature",
                        logics = e.message
                    )
                }
                else -> {
                    ExtractUserResponseFromServer.unauthorized(
                        errorCode = MSAServiceErrorCode.INVALID_TOKEN,
                        errorMessage = "Invalid token",
                        logics = e.message
                    )
                }
            }
        } catch (e: Exception) {
            logger.error(e) { "Unexpected error during JWT validation" }
            return ExtractUserResponseFromServer.internalError(
                errorMessage = "Token validation failed: ${e.message}",
                logics = e.stackTraceToString()
            )
        }
    }

    fun validateTokenAndExtractAdvertiserInfo(
        token: String,
    ): ExtractUserResponseFromServer {
        logger.debug { "Starting JWT token validation advertiser token" }

        try {
            // JWT 디코딩 및 검증 (서명, iss, exp 자동 검증)
            val jwt: Jwt = jwtDecoder.decode(token)

            // 추가 검증: Issuer 확인
            val expectedIssuer = "https://cognito-idp.$region.amazonaws.com/$userPoolId"
            val actualIssuer = jwt.issuer?.toString()

            if (actualIssuer != expectedIssuer) {
                logger.warn { "Invalid issuer: expected=$expectedIssuer, actual=$actualIssuer" }
                return ExtractUserResponseFromServer.unauthorized(
                    errorCode = MSAServiceErrorCode.INVALID_ISSUER,
                    errorMessage = "Invalid token issuer",
                    logics = "Expected issuer: $expectedIssuer, Actual: $actualIssuer"
                )
            }

            // 추가 검증: Client ID 확인 (aud 또는 client_id)
            val tokenClientId = jwt.claims["client_id"] as? String ?: jwt.audience?.firstOrNull()
            if (tokenClientId != clientId) {
                logger.warn { "Invalid client_id: expected=$clientId, actual=$tokenClientId" }
                return ExtractUserResponseFromServer.unauthorized(
                    errorCode = MSAServiceErrorCode.INVALID_AUDIENCE,
                    errorMessage = "Invalid token audience",
                    logics = "Expected client_id: $clientId, Actual: $tokenClientId"
                )
            }

            // 사용자 정보 추출
            val extractedUser = extractUserFromToken(jwt)

            // 추가 검증: 프론트엔드에서 기대하는 userType과 일치하는지 확인
            if (extractedUser.userType != "ADVERTISER") {
                logger.warn {
                    "UserType mismatch: expected=ADVERTISER " +
                            "actual=${extractedUser.userType}, userId=${extractedUser.userId}"
                }
                return ExtractUserResponseFromServer.forbidden(
                    errorCode = MSAServiceErrorCode.INVALID_USER_TYPE,
                    errorMessage = "User type does not match the expected type for this service",
                    logics = "Expected userType: ADVERTISER, Actual: ${extractedUser.userType}"
                )
            }

            logger.info { "Token validated successfully for user:" +
                    " ${extractedUser.userId}, userType: ${extractedUser.userType}" }

            return ExtractUserResponseFromServer.success(
                user = extractedUser,
                logics = "Token validation successful for userType: ${extractedUser.userType}"
            )

        } catch (e: JwtException) {
            logger.error(e) { "JWT validation failed" }
            return when {
                e.message?.contains("expired", ignoreCase = true) == true -> {
                    ExtractUserResponseFromServer.unauthorized(
                        errorCode = MSAServiceErrorCode.EXPIRED_TOKEN,
                        errorMessage = "Token has expired",
                        logics = e.message
                    )
                }
                e.message?.contains("signature", ignoreCase = true) == true -> {
                    ExtractUserResponseFromServer.unauthorized(
                        errorCode = MSAServiceErrorCode.INVALID_SIGNATURE,
                        errorMessage = "Invalid token signature",
                        logics = e.message
                    )
                }
                else -> {
                    ExtractUserResponseFromServer.unauthorized(
                        errorCode = MSAServiceErrorCode.INVALID_TOKEN,
                        errorMessage = "Invalid token",
                        logics = e.message
                    )
                }
            }
        } catch (e: Exception) {
            logger.error(e) { "Unexpected error during JWT validation" }
            return ExtractUserResponseFromServer.internalError(
                errorMessage = "Token validation failed: ${e.message}",
                logics = e.stackTraceToString()
            )
        }
    }
    /**
     * JWT에서 사용자 정보를 추출합니다.
     */
    private fun extractUserFromToken(jwt: Jwt): ExtractedUserFromToken {
        val userId = jwt.subject ?: throw JwtException("Token missing 'sub' claim")

        return ExtractedUserFromToken(
            userId = userId,
            email = jwt.claims["email"] as? String,
            emailVerified = jwt.claims["email_verified"] as? Boolean,
            phoneNumber = jwt.claims["phone_number"] as? String,
            phoneNumberVerified = jwt.claims["phone_number_verified"] as? Boolean,
            name = jwt.claims["name"] as? String,
            userType = jwt.claims["custom:userType"] as? String
        )
    }

    /**
     * 토큰에서 Bearer 접두사를 제거합니다.
     *
     * @param authorizationHeader Authorization 헤더 값 (예: "Bearer eyJ...")
     * @return Bearer 접두사가 제거된 토큰
     * @throws IllegalArgumentException Authorization 헤더가 올바르지 않은 경우
     */
    fun extractTokenFromHeader(authorizationHeader: String?): String {
        if (authorizationHeader.isNullOrBlank()) {
            throw IllegalArgumentException("Authorization header is missing")
        }

        if (!authorizationHeader.startsWith("Bearer ", ignoreCase = true)) {
            throw IllegalArgumentException("Authorization header must start with 'Bearer '")
        }

        return authorizationHeader.substring(7).trim()
    }

    /**
     * 사용자가 특정 역할을 가지고 있는지 확인합니다.
     *
     * @param user 사용자 정보
     * @param requiredUserType 필요한 사용자 타입
     * @return 역할이 일치하면 true
     */
    fun hasRequiredRole(user: ExtractedUserFromToken, requiredUserType: String): Boolean {
        return user.userType == requiredUserType
    }
}