# Marketing Auth API Server

AWS Cognito JWT 토큰을 검증하는 전용 API 서버입니다.

## 주요 기능

- AWS Cognito ID Token 검증
- 사용자 정보 추출 (email, name, userType 등)
- 역할 기반 접근 제어 (INFLUENCER, ADVERTISER_COMMON)
- CORS 지원

## 기술 스택

- Kotlin 1.9.25
- Spring Boot 3.5.7
- Spring Security 6.x
- OAuth2 Resource Server
- Gradle 8.14.3

## 시작하기

### 1. 환경 변수 설정

`.env.example` 파일을 참고하여 환경 변수를 설정합니다:

```bash
# .env 파일 생성
cp .env.example .env

# 실제 Cognito 정보로 수정
# AWS_REGION=ap-northeast-2
# COGNITO_USER_POOL_ID=ap-northeast-2_xxxxx
# COGNITO_CLIENT_ID=your-client-id
```

또는 `application.yaml`에서 직접 수정할 수 있습니다.

### 2. 빌드 및 실행

```bash
# 빌드
./gradlew build

# 실행
./gradlew bootRun
```

또는 환경 변수와 함께 실행:

```bash
AWS_REGION=ap-northeast-2 \
COGNITO_USER_POOL_ID=ap-northeast-2_xxxxx \
COGNITO_CLIENT_ID=your-client-id \
./gradlew bootRun
```

## API 엔드포인트

### 1. 헬스 체크

```http
GET /api/auth/health
```

인증 없이 접근 가능한 헬스 체크 엔드포인트입니다.

**응답 예시:**
```json
{
  "status": "UP",
  "service": "marketing-auth-api-server"
}
```

### 2. 토큰 검증

```http
GET /api/auth/validate
Authorization: Bearer {ID_TOKEN}
```

JWT 토큰을 검증하고 사용자 정보를 반환합니다.

**응답 예시:**
```json
{
  "success": true,
  "data": {
    "message": "Token is valid",
    "user": {
      "sub": "uuid",
      "email": "user@example.com",
      "name": "홍길동",
      "userType": "INFLUENCER",
      "exp": 1234567890,
      "iat": 1234567800
    },
    "userType": "INFLUENCER"
  }
}
```

### 3. 광고주 전용 엔드포인트

```http
POST /api/auth/validate/advertiser
Authorization: Bearer {ID_TOKEN}
```

`userType`이 `ADVERTISER_COMMON`인 사용자만 접근 가능합니다.

**성공 응답:**
```json
{
  "success": true,
  "data": {
    "message": "Advertiser access granted",
    "user": { ... },
    "userType": "ADVERTISER_COMMON"
  }
}
```

**실패 응답 (권한 없음):**
```json
{
  "success": false,
  "error": {
    "message": "광고주만 접근 가능합니다.",
    "code": "FORBIDDEN",
    "details": "Required userType: ADVERTISER_COMMON, Current: INFLUENCER"
  }
}
```

### 4. 인플루언서 전용 엔드포인트

```http
GET /api/auth/validate/influencer
Authorization: Bearer {ID_TOKEN}
```

`userType`이 `INFLUENCER`인 사용자만 접근 가능합니다.

## 프론트엔드 통합

### React + Amplify 예시

```javascript
import { fetchAuthSession } from 'aws-amplify/auth';

async function validateToken() {
  try {
    // Amplify에서 ID Token 가져오기
    const { tokens } = await fetchAuthSession();
    const idToken = tokens.idToken.toString();

    // API 호출
    const response = await fetch('http://localhost:8080/api/auth/validate', {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${idToken}`,
        'Content-Type': 'application/json'
      }
    });

    const data = await response.json();

    if (data.success) {
      console.log('User info:', data.data.user);
      console.log('User type:', data.data.userType);
    } else {
      console.error('Validation failed:', data.error);
    }
  } catch (error) {
    console.error('API call failed:', error);
  }
}
```

## 검증 과정

이 서버는 다음과 같은 단계로 JWT를 검증합니다:

1. **서명 검증**: Cognito JWKS 공개 키로 JWT 서명 검증
2. **Issuer 검증**: `iss` 클레임이 Cognito User Pool과 일치하는지 확인
3. **Audience 검증**: `aud` 또는 `client_id`가 설정된 Client ID와 일치하는지 확인
4. **만료 시간 검증**: `exp` 클레임이 현재 시간 이후인지 확인
5. **사용자 정보 추출**: `custom:userType` 등의 사용자 정보 추출

## 보안 고려사항

### CORS 설정

프로덕션 환경에서는 `SecurityConfig.kt`의 CORS 설정을 실제 프론트엔드 도메인으로 제한해야 합니다:

```kotlin
configuration.allowedOrigins = listOf(
    "https://your-production-domain.com"
)
```

### HTTPS 사용

프로덕션 환경에서는 반드시 HTTPS를 사용해야 합니다.

### 환경 변수 보안

- `.env` 파일은 절대 Git에 커밋하지 마세요
- Cognito 정보는 환경 변수나 AWS Secrets Manager를 통해 관리하세요

## 문제 해결

### 빌드 오류

의존성 문제가 발생하면:

```bash
./gradlew clean build --refresh-dependencies
```

### JWT 검증 실패

1. Cognito User Pool ID가 올바른지 확인
2. Client ID가 올바른지 확인
3. 토큰이 만료되지 않았는지 확인
4. JWKS 엔드포인트에 접근 가능한지 확인

### CORS 오류

`SecurityConfig.kt`의 `allowedOrigins`에 프론트엔드 도메인이 포함되어 있는지 확인하세요.

## 프로젝트 구조

```
src/main/kotlin/org/example/authapiserver/
├── config/
│   └── SecurityConfig.kt          # Spring Security 설정
├── controller/
│   └── AuthValidationController.kt # API 엔드포인트
├── service/
│   └── CognitoValidationService.kt # JWT 검증 로직
├── dto/
│   ├── UserInfo.kt                 # 사용자 정보 DTO
│   └── ApiResponse.kt              # API 응답 DTO
└── AuthApiServerApplication.kt     # 메인 애플리케이션
```

## 라이선스

Copyright © 2025. All rights reserved.