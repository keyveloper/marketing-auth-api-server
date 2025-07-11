package org.example.authapiserver.dto

import java.util.UUID

/**
 * Cognito JWT 토큰에서 추출한 사용자 정보
 *
 * ID Token에 포함된 모든 사용자 관련 클레임을 담고 있습니다.
 */
data class ExtractedUserFromToken(
    val userId: UUID,                     // Cognito User ID (sub - UUID)
    val email: String?,                     // 이메일
    val emailVerified: Boolean?,            // 이메일 인증 여부 (email_verified)
    val phoneNumber: String?,               // 전화번호 (phone_number)
    val phoneNumberVerified: Boolean?,      // 전화번호 인증 여부 (phone_number_verified)
    val name: String?,                      // 이름
    val userType: String?                   // 유저 타입 (custom:userType - INFLUENCER 또는 ADVERTISER_COMMON)
)
