package org.example.authapiserver.controller

import io.github.oshai.kotlinlogging.KotlinLogging
import org.example.authapiserver.enums.MSAServiceErrorCode
import org.example.authapiserver.dto.ExtractUserResponseFromServer
import org.example.authapiserver.service.CognitoValidationService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

private val logger = KotlinLogging.logger {}

/**
 * JWT 토큰 검증 컨트롤러
 *
 * 프론트엔드에서 전송된 Cognito JWT 토큰을 검증하고
 * 사용자 정보 및 권한을 확인하는 API 엔드포인트를 제공합니다.
 */
@RestController
@RequestMapping("/api/auth")
class AuthValidationController(
    private val cognitoValidationService: CognitoValidationService
) {

    /**
     * 인플루언서 전용 엔드포인트
     *
     * userType이 INFLUENCER인지 검증합니다.
     * Service 레벨에서 expectedUserType 검증을 수행합니다.
     *
     * @param authorization Authorization 헤더 (Bearer 토큰)
     * @return 검증 결과
     */
    @GetMapping("/validate/influencer")
    fun validateInfluencer(
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<ExtractUserResponseFromServer> {
        logger.info { "Influencer validation requested" }

        return try {
            val token = cognitoValidationService.extractTokenFromHeader(authorization)
            val response = cognitoValidationService.validateTokenAndExtractInfluencerInfo(
                token = token,
            )

            ResponseEntity.status(response.httpStatus).body(response)

        } catch (e: IllegalArgumentException) {
            logger.warn { "Invalid authorization header: ${e.message}" }
            val errorResponse = ExtractUserResponseFromServer.unauthorized(
                errorCode = MSAServiceErrorCode.INVALID_HEADER,
                errorMessage = "Invalid authorization header",
                logics = e.message
            )
            ResponseEntity.status(errorResponse.httpStatus).body(errorResponse)
        } catch (e: Exception) {
            logger.error(e) { "Unexpected error during influencer validation" }
            val errorResponse = ExtractUserResponseFromServer.internalError(
                errorMessage = "Influencer validation failed: ${e.message}",
                logics = e.stackTraceToString()
            )
            ResponseEntity.status(errorResponse.httpStatus).body(errorResponse)
        }
    }

    /**
     * 인플루언서 전용 엔드포인트
     *
     * userType이 ADVERTISER인지 검증합니다.
     * Service 레벨에서 expectedUserType 검증을 수행합니다.
     *
     * @param authorization Authorization 헤더 (Bearer 토큰)
     * @return 검증 결과
     */
    @GetMapping("/validate/advertiser")
    fun validateAdvertiser(
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<ExtractUserResponseFromServer> {
        logger.info { "Advertiser validation requested" }

        return try {
            val token = cognitoValidationService.extractTokenFromHeader(authorization)
            val response = cognitoValidationService.validateTokenAndExtractAdvertiserInfo(
                token = token,
            )

            ResponseEntity.status(response.httpStatus).body(response)

        } catch (e: IllegalArgumentException) {
            logger.warn { "Invalid authorization header: ${e.message}" }
            val errorResponse = ExtractUserResponseFromServer.unauthorized(
                errorCode = MSAServiceErrorCode.INVALID_HEADER,
                errorMessage = "Invalid authorization header",
                logics = e.message
            )
            ResponseEntity.status(errorResponse.httpStatus).body(errorResponse)
        } catch (e: Exception) {
            logger.error(e) { "Unexpected error during influencer validation" }
            val errorResponse = ExtractUserResponseFromServer.internalError(
                errorMessage = "Influencer validation failed: ${e.message}",
                logics = e.stackTraceToString()
            )
            ResponseEntity.status(errorResponse.httpStatus).body(errorResponse)
        }
    }

    /**
     * 헬스 체크 엔드포인트
     */
    @GetMapping("/health")
    fun health(): ResponseEntity<Map<String, String>> {
        return ResponseEntity.ok(
            mapOf(
                "status" to "UP",
                "service" to "marketing-auth-api-server"
            )
        )
    }
}