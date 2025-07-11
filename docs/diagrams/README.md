# 시퀀스 다이어그램

이 디렉토리에는 마케팅 플랫폼 인증 처리 흐름을 설명하는 PlantUML 시퀀스 다이어그램이 포함되어 있습니다.

## 파일 목록

- `auth-flow.puml`: 인플루언서(INFLUENCER) 인증 처리 전체 흐름 (Sequence Diagram)
- `advertiser-auth-flow.puml`: 광고주(ADVERTISER) 인증 처리 흐름 (Sequence Diagram)
- `data-flow.puml`: 마케팅 플랫폼 전체 데이터 흐름도 (Component Diagram)
- `process-data-flow.puml`: 프로세스 중심 데이터 흐름도 (DFD - Data Flow Diagram)

## 다이어그램 확인 방법

### 방법 1: IntelliJ IDEA 플러그인 사용 (추천)

1. IntelliJ IDEA에서 **PlantUML integration** 플러그인 설치
   - `File` → `Settings` → `Plugins`
   - "PlantUML integration" 검색 및 설치
   - IDE 재시작

2. `.puml` 파일 열기
   - 파일을 열면 오른쪽에 자동으로 다이어그램 미리보기가 표시됩니다
   - 코드 수정 시 실시간으로 다이어그램이 업데이트됩니다

3. 이미지로 내보내기
   - 다이어그램 미리보기에서 우클릭
   - "Save Diagram" 선택
   - PNG, SVG 등 원하는 포맷 선택

### 방법 2: 온라인 에디터 사용

1. [PlantUML 온라인 에디터](https://www.plantuml.com/plantuml/uml/) 접속
2. `.puml` 파일의 내용을 복사하여 붙여넣기
3. 자동으로 다이어그램이 렌더링됩니다
4. PNG, SVG로 다운로드 가능

### 방법 3: Visual Studio Code 확장

1. VSCode에서 **PlantUML** 확장 설치
2. `.puml` 파일 열기
3. `Alt + D`를 눌러 미리보기 활성화

### 방법 4: CLI로 이미지 생성

```bash
# PlantUML JAR 다운로드
# https://plantuml.com/download

# PNG 생성
java -jar plantuml.jar auth-flow.puml

# SVG 생성
java -jar plantuml.jar -tsvg auth-flow.puml
```

## 다이어그램 설명

### auth-flow.puml
전체 인증 처리 흐름을 상세하게 설명합니다:

1. **회원가입/로그인 단계**
   - React Frontend에서 AWS Amplify를 통한 Cognito 로그인
   - ID Token(JWT) 발급 및 저장

2. **API 요청 및 인증 단계**
   - Authorization 헤더에 Bearer Token 포함하여 요청
   - Auth API Server의 `/api/auth/validate/influencer` 엔드포인트 호출

3. **토큰 검증 단계**
   - JWT 디코딩 및 서명 검증
   - JWKS 공개키를 통한 RSA 서명 검증
   - Issuer 및 Client ID 검증

4. **사용자 정보 추출 및 검증**
   - JWT Claims에서 사용자 정보 추출
   - UserType 검증 (INFLUENCER 확인)

5. **예외 처리**
   - 만료된 토큰, 서명 오류, UserType 불일치 등

### advertiser-auth-flow.puml
광고주 전용 인증 흐름:

- `/api/auth/validate/advertiser` 엔드포인트 사용
- UserType이 "ADVERTISER"로 시작하는지 검증
- ADVERTISER, ADVERTISER_PREMIUM, ADVERTISER_ENTERPRISE 등 허용

### data-flow.puml
전체 시스템 데이터 흐름:

1. **로그인 데이터 흐름**
   - React Frontend에서 AWS Cognito로 인증 요청
   - JWT Token 발급 및 저장 (LocalStorage)

2. **API 요청 데이터 흐름**
   - Frontend → Auth API Server로 Bearer Token 포함 요청
   - JWT 디코딩 및 검증 (Signature, Issuer, Client ID)
   - 사용자 정보 추출 (userId, email, userType 등)

3. **컴포넌트 간 데이터 교환**
   - Controller ↔ Service ↔ JwtDecoder
   - Cognito JWKS 공개키 조회 (캐싱)
   - Configuration 설정 값 주입

4. **에러 데이터 반환**
   - 401: 인증 실패 (만료, 서명 오류 등)
   - 403: 권한 없음 (UserType 불일치)
   - 500: 서버 내부 오류

### process-data-flow.puml
프로세스 중심 데이터 흐름도 (DFD):

**DFD 구성 요소:**
- **외부 엔티티** (파란색): 인플루언서, 광고주
- **프로세스** (초록색): P1~P11 (각 데이터 처리 단계)
- **데이터 저장소** (노란색): D1~D4 (Cognito, JWKS, Config, LocalStorage)
- **데이터 흐름** (화살표): 데이터의 이동 경로

**11개 프로세스:**
1. P1: 사용자 인증 (Cognito Login)
2. P2: 토큰 발급 (JWT Issuer)
3. P3: 토큰 저장 (LocalStorage)
4. P4: API 요청 생성 (Add Bearer Token)
5. P5: 토큰 검증 요청 (Controller)
6. P6: 토큰 디코딩 (JwtDecoder)
7. P7: 서명 검증 (JWKS RSA)
8. P8: 클레임 검증 (Issuer/ClientId)
9. P9: 사용자 정보 추출 (Extract User)
10. P10: UserType 검증 (Role Check)
11. P11: 응답 생성 (Response Builder)

**4개 데이터 저장소:**
- D1: Cognito User Pool (사용자 인증 정보)
- D2: JWKS 공개키 저장소 (RSA 공개키 캐싱)
- D3: Application Configuration (설정 파일)
- D4: Browser LocalStorage (JWT 토큰)

## 코드 참조

다이어그램의 각 단계는 실제 코드 위치를 주석으로 표시하고 있습니다:

- `AuthValidationController.kt`: API 엔드포인트 정의
- `CognitoValidationService.kt`: 토큰 검증 로직
- Spring Security OAuth2 JWT: 자동 서명 검증

## 다이어그램 수정

`.puml` 파일을 직접 수정하여 다이어그램을 업데이트할 수 있습니다.

**주요 문법:**
```plantuml
actor "이름" as Alias          # 액터 정의
participant "이름" as Alias     # 참여자 정의
-> : 메시지                     # 동기 메시지
--> : 응답                      # 응답 메시지
activate/deactivate            # 활성화 바
note left/right: 설명          # 노트 추가
alt/else/end                   # 조건 분기
```

더 자세한 문법은 [PlantUML 공식 문서](https://plantuml.com/sequence-diagram)를 참조하세요.
