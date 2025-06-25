package org.example.authapiserver.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

/**
 * Spring Security 설정
 *
 * JWT 토큰 기반 인증을 설정하고 CORS를 구성합니다.
 */
@Configuration
@EnableWebSecurity
class SecurityConfig {

    /**
     * Security Filter Chain 설정
     *
     * - /api/auth/health: 인증 없이 접근 가능 (헬스 체크)
     * - 기타 모든 요청: JWT 인증 필요 없음 (컨트롤러에서 수동으로 검증)
     * - CSRF 비활성화 (REST API)
     * - Session 사용 안 함 (Stateless)
     * - CORS 활성화
     */
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource()) }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { authorize ->
                authorize
                    .requestMatchers("/api/auth/health", "/actuator/**").permitAll()
                    .anyRequest().permitAll() // 수동 검증을 위해 모든 요청 허용
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { }
            }

        return http.build()
    }

    /**
     * CORS 설정
     *
     * 프론트엔드에서 API를 호출할 수 있도록 CORS를 허용합니다.
     * 프로덕션 환경에서는 allowedOrigins를 특정 도메인으로 제한해야 합니다.
     */
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()

        // 허용할 origin (프로덕션에서는 실제 도메인으로 변경)
        configuration.allowedOrigins = listOf(
            "http://localhost:3000",
            "http://localhost:5173",
            "http://localhost:8080"
            // 프로덕션 도메인 추가
        )

        // 허용할 HTTP 메서드
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")

        // 허용할 헤더
        configuration.allowedHeaders = listOf("*")

        // 인증 정보 허용
        configuration.allowCredentials = true

        // 노출할 헤더
        configuration.exposedHeaders = listOf("Authorization")

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)

        return source
    }

    /**
     * JWT Decoder Bean
     *
     * Spring Security가 JWT를 검증할 때 사용하는 디코더입니다.
     * Cognito의 JWKS 엔드포인트를 사용하여 공개 키를 가져옵니다.
     */
    @Bean
    fun jwtDecoder(
        @Value("\${cognito.region}") region: String,
        @Value("\${cognito.userPoolId}") userPoolId: String
    ): JwtDecoder {
        val jwkSetUri = "https://cognito-idp.$region.amazonaws.com/$userPoolId/.well-known/jwks.json"
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build()
    }
}
