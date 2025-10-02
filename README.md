# OAuth 2.0 Authentication Module Monorepo

Turborepo 기반의 풀스택 OAuth 2.0 인증/인가 모듈입니다. Next.js(TypeScript) 프론트엔드와 Spring Boot 3(Java 17) 백엔드로 구성되어 있으며, 공통 UI 컴포넌트는 별도의 패키지로 분리해 확장성을 확보했습니다.

## 프로젝트 구조

```
.
├── infra/             # 배포/인프라 구성물을 위한 자리
├── backend/           # Spring Boot 3 OAuth2 서버
├── frontend/          # Next.js 14 프론트엔드 (App Router)
│   └── packages/ui    # 재사용 가능한 React UI 컴포넌트
├── turbo.json         # Turborepo 파이프라인 구성
└── pnpm-workspace.yaml
```

## 사전 요구 사항

- Node.js 18 이상
- pnpm 8 이상 (`pnpm --version`으로 확인)
- Java 17 JDK
- Gradle 8 이상 (또는 `gradle wrapper` 실행 후 생성된 래퍼 사용)

소셜 로그인 성공 시 백엔드는 `/oauth/callback#access_token=...&refresh_token=...` 으로 리디렉션하며, 프론트는 이 경로에서 토큰을 저장한 뒤 홈으로 이동합니다.

## 설치 및 실행

```bash
pnpm install

# 프론트엔드만 실행
pnpm --filter @repo/frontend dev

# 전체 파이프라인 실행 (turbo dev)
pnpm dev
```

백엔드는 별도로 실행합니다.

```bash
cd backend
# 로컬에 gradle이 있다면
gradle bootRun
# 또는 포함된 gradle wrapper 사용
./gradlew bootRun
```

## 환경 변수

### 프론트엔드 (`frontend`)

`.env.local` 등에 다음 값을 설정합니다.

| 변수 | 설명 |
| ---- | ---- |
| `NEXT_PUBLIC_API_BASE_URL` | 백엔드 서버 주소 (예: `http://localhost:8080`) |
> **참고**: `NEXT_PUBLIC_API_BASE_URL` 값을 지정하지 않으면 소셜 로그인 버튼이 비활성화되고 토큰 요청이 실패합니다.

### 백엔드 (`backend`)

`application.yml`은 PostgreSQL을 기본 대상으로 하도록 구성되어 있으며, 필요 시 다음 환경 변수를 정의하세요.

| 변수 | 설명 |
| ---- | ---- |
| `SERVER_PORT` | 서버 포트 (기본값 8080) |
| `SPRING_DATASOURCE_URL` / `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD` | PostgreSQL 연결 정보 |
| `SPRING_DATASOURCE_DRIVER` | JDBC 드라이버 클래스 (기본 `org.postgresql.Driver`) |
| `SPRING_JPA_HIBERNATE_DIALECT` | 필요 시 Dialect 재정의 (기본 `org.hibernate.dialect.PostgreSQLDialect`) |
| `FRONTEND_BASE_URL` | OAuth 로그인 성공 후 리디렉션될 프론트엔드 주소 (기본 `http://localhost:3000`) |
| `OAUTH_GOOGLE_CLIENT_ID` / `OAUTH_GOOGLE_CLIENT_SECRET` | Google OAuth 클라이언트 |
| `OAUTH_KAKAO_CLIENT_ID` / `OAUTH_KAKAO_CLIENT_SECRET` | Kakao OAuth 클라이언트 |
| `OAUTH_NAVER_CLIENT_ID` / `OAUTH_NAVER_CLIENT_SECRET` | Naver OAuth 클라이언트 (선택) |
| `OAUTH_APPLE_CLIENT_ID` / `OAUTH_APPLE_CLIENT_SECRET` | Apple OAuth 클라이언트 (선택) |
| `OAUTH_*_REDIRECT_URI` | 필요 시 사용자 지정 리디렉션 URL |
| `JWT_SECRET` | Access/Refresh 토큰 서명을 위한 비밀 키 |
| `JWT_ACCESS_TOKEN_TTL` | Access Token 유효시간(초, 기본 900초) |
| `JWT_REFRESH_TOKEN_TTL` | Refresh Token 유효시간(초, 기본 1209600초) |

`backend/src/main/resources/secret.yaml`을 통해 민감한 값을 분리해 둘 수 있으며, `application.yml`에서 자동으로 import 됩니다.

## 주요 기능

- **일반 회원가입/로그인**: 이메일 혹은 닉네임과 비밀번호로 인증. 비밀번호는 BCrypt로 암호화합니다.
- **OAuth 2.0 소셜 로그인**: Google, Kakao, Naver, Apple 지원. OAuth 완료 직후 Access/Refresh Token을 발급해 즉시 로그인시키고, 미완성 프로필은 홈 화면에서 배너로 안내합니다.
- **추가 정보 스텝**: 로그인 직후에도 계정을 바로 사용할 수 있으며, 필요 시 프로필 페이지에서 자기소개 등 추가 정보를 수집하도록 유도할 수 있습니다.
- **JWT 기반 인증**: Access Token + Refresh Token 구조로 세션 없이 인증을 유지하고, 토큰 만료 시 자동으로 재발급합니다.
- **Tailwind 디자인 시스템**: Tailwind CSS + PostCSS 환경으로 프론트 전반에 일관된 톤앤매너와 반응형 레이아웃을 제공합니다.
- **공통 UI 컴포넌트**: `frontend/packages/ui`에 버튼/카드/인풋 등을 정의하여 추가 애플리케이션에서도 쉽게 조합 가능합니다.

## API 개요

| 메소드 | 경로 | 설명 |
| ------ | ---- | ---- |
| `POST` | `/api/auth/register` | 일반 회원가입 |
| `POST` | `/api/auth/login` | 일반 로그인 (JWT 발급) |
| `POST` | `/api/auth/refresh` | Refresh Token으로 Access Token 재발급 |
| `POST` | `/api/auth/logout` | Refresh Token 폐기 |
| `GET`  | `/api/auth/me` | 현재 로그인 사용자 정보 조회 |

응답은 공통 포맷 `ApiResponse<T>` (`success`, `message`, `data`)를 사용합니다.

## 확장 가이드

- 새로운 프론트엔드 앱을 추가하려면 `frontend` 디렉터리 내에서 패키지를 확장하고, 필요 시 `frontend/packages` 하위에 UI 패키지를 추가하세요.
- 공통 UI가 필요하면 `frontend/packages/ui/src/components`에 컴포넌트를 작성하고 `src/index.ts`에 export를 추가하세요.
- 새로운 OAuth 제공자를 붙일 때는 `AuthProvider` enum과 `CustomOAuth2UserService`의 추출 로직을 확장하면 됩니다.
- 모듈 확장을 위해 모든 비즈니스 로직을 `backend`의 `AuthService`에 캡슐화했고, 컨트롤러는 DTO ↔ 서비스 변환만 담당합니다.

## 테스트

현재 자동 테스트는 포함되어 있지 않습니다. 필요한 경우 `backend`에서 JUnit 테스트를 추가하거나, 프론트엔드에서 Playwright/Testing Library를 연결해 주세요.
