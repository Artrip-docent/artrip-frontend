# Artrip Android App

AI 기반 미술 전시 도슨트 서비스의 Android 클라이언트입니다.

## 앱 실행 전 필수 설정

서버 IP 주소를 아래 두 파일에서 실제 서버 주소로 변경해야 합니다.

- `app/src/main/java/com/example/docent/RetrofitClient.kt` — `BASE_URL` 수정
- `app/src/main/res/xml/network_security_config.xml` — 허용 도메인 수정

## 기술 스택

| 분류 | 기술 |
|------|------|
| 언어 | Kotlin |
| 최소 SDK | API 24 (Android 7.0) |
| 타겟 SDK | API 34 (Android 14) |
| 아키텍처 | MVVM, Data Binding, View Binding |
| 네트워크 | Retrofit 2.9.0, OkHttp 4.12.0 |
| 이미지 로딩 | Glide 4.15.1 |
| 카메라 | CameraX (lifecycle, camera2, view) |
| QR / 바코드 | ZXing (journeyapps) 4.3.0 |
| 차트 | MPAndroidChart 3.1.0 |
| 직렬화 | Gson 2.10.1 |
| UI | Material Components 1.12.0 |

## 프로젝트 구조

```
artrip-frontend/
├── app/
│   └── src/main/
│       ├── java/com/example/docent/   # Kotlin 소스 (44개 파일)
│       └── res/                       # 레이아웃, 리소스
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

## 주요 화면

| 화면 | 설명 |
|------|------|
| `MainActivity` | 앱 진입점 |
| `LoginActivity` | 이메일/비밀번호 로그인 |
| `CameraActivity` | 작품 촬영 (CLIP 검색용) |
| `ChatActivity` | AI 도슨트 채팅 인터페이스 |
| `ChatHistoryActivity` | 이전 대화 기록 조회 |
| `ExhibitionSelectActivity` | 전시 목록 탐색 및 선택 |
| `ArtRecommendationActivity` | AI 추천 작품 목록 |
| `Keyword1Activity` | 선호 키워드 선택 |
| `PreferenceanalysisActivity` | 선호도 분석 결과 (차트) |
| `MypageActivity` | 마이페이지 (관람 이력, 즐겨찾기) |
| `GoodexhibitionActivity` | 좋아요한 전시 목록 |
| `ReviewWriteActivity` | 전시 리뷰 작성 |
| `ProfileEditActivity` | 닉네임·프로필 이미지 수정 |
| `artworkviewingActivity` | 전시 내 관람 작품 목록 |
| `exhibitionviewingActivity` | 전시 상세 정보 |
| `communityActivity` | 커뮤니티 기능 |

## 핵심 기능

### 1. 카메라 기반 작품 검색
CameraX로 작품을 촬영하면 서버의 CLIP 모델이 유사 작품 3개를 반환합니다.

### 2. AI 도슨트 채팅
SSE(Server-Sent Events) 스트리밍으로 GPT 응답을 실시간으로 수신하여 채팅 UI에 표시합니다.

### 3. 선호도 분석 시각화
MPAndroidChart를 활용해 사용자의 예술 스타일·감성 선호도를 차트로 표현합니다.

### 4. 전시 탐색 및 리뷰
전시 검색, 좋아요, 별점 리뷰 작성을 지원합니다.

### 5. 관람 이력 관리
방문한 전시와 조회한 작품 이력을 자동으로 기록합니다.

## 빌드 및 실행

### 요구사항
- Android Studio Hedgehog (2023.1.1) 이상
- JDK 17 이상
- Android SDK API 34

### 실행 방법

1. Android Studio에서 프로젝트를 열기
2. `RetrofitClient.kt`의 `BASE_URL`을 서버 주소로 변경
3. `network_security_config.xml`에 서버 도메인/IP 추가
4. Gradle Sync 후 실행 (`Run > Run 'app'`)

### Gradle 빌드

```bash
./gradlew assembleDebug      # 디버그 APK 빌드
./gradlew assembleRelease    # 릴리즈 APK 빌드
./gradlew installDebug       # 연결된 기기에 디버그 설치
```

## 권한

앱이 사용하는 Android 권한:

| 권한 | 용도 |
|------|------|
| `CAMERA` | 작품 사진 촬영 |
| `INTERNET` | API 서버 통신 |
| `READ_EXTERNAL_STORAGE` | 갤러리 이미지 접근 |
| `RECORD_AUDIO` | 음성 입력 (선택) |

## 인증 방식

- **이메일/비밀번호 로그인**: JWT 토큰 발급 후 `SharedPreferences`에 저장
- **Kakao OAuth**: 카카오 SDK를 통한 소셜 로그인, 서버에서 JWT 교환
