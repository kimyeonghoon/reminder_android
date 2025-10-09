# 📱 Play Store 출시 가이드

> Reminder 앱을 Google Play Store에 출시하기 위한 완벽 가이드

## 📋 목차

- [출시 전 체크리스트](#출시-전-체크리스트)
- [앱 정보 (Store Listing)](#앱-정보-store-listing)
- [스크린샷 가이드라인](#스크린샷-가이드라인)
- [개인정보 처리방침](#개인정보-처리방침)
- [릴리즈 빌드](#릴리즈-빌드)
- [APK/AAB 생성](#apkaab-생성)
- [테스트 트랙](#테스트-트랙)
- [스토어 등록 절차](#스토어-등록-절차)

## 출시 전 체크리스트

### 1. 기술적 요구사항

- [ ] **최소 SDK 버전**: API 26 (Android 8.0) 이상
- [ ] **타겟 SDK 버전**: API 34 (Android 14) - Play Store 요구사항
- [ ] **앱 서명**: 릴리즈 키스토어 생성 및 안전 보관
- [ ] **ProGuard**: 코드 난독화 및 최적화 활성화
- [ ] **권한**: 필수 권한만 요청, 런타임 권한 처리
- [ ] **64비트 지원**: arm64-v8a, x86_64 ABI 포함

### 2. 품질 기준

- [ ] **크래시 없음**: 주요 사용 시나리오에서 크래시 없음
- [ ] **ANR 없음**: 5초 이상 응답 없음 현상 없음
- [ ] **메모리 누수**: 프로파일러로 메모리 누수 확인
- [ ] **배터리 사용량**: 백그라운드 배터리 사용 최소화
- [ ] **접근성**: TalkBack으로 모든 기능 사용 가능
- [ ] **다크 모드**: 시스템 다크 모드 완벽 지원

### 3. 콘텐츠 정책 준수

- [ ] **저작권**: 모든 이미지, 아이콘, 폰트 라이선스 확인
- [ ] **개인정보**: GDPR 및 한국 개인정보보호법 준수
- [ ] **광고**: 광고 포함 시 AdMob 정책 준수
- [ ] **유해 콘텐츠**: 폭력, 성인, 혐오 콘텐츠 없음

### 4. 문서화

- [ ] **README.md**: 프로젝트 설명 및 설치 방법
- [ ] **CHANGELOG.md**: 버전별 변경 사항
- [ ] **개인정보 처리방침**: 웹 호스팅된 URL
- [ ] **앱 내 도움말**: 사용 가이드 제공

## 앱 정보 (Store Listing)

### 앱 제목 (최대 30자)

```
할 일 관리 - 스마트 리마인더
```

또는

```
Reminder - 할 일 관리
```

### 짧은 설명 (최대 80자)

```
70대도 쉽게 사용하는 스마트한 할 일 관리. TTS, 음성 입력, 간편 모드 지원.
```

### 전체 설명 (최대 4,000자)

```markdown
📝 할 일 관리 - 누구나 쉽게 사용하는 스마트 리마인더

70대 어르신도 쉽게 사용할 수 있도록 설계된 할 일 관리 앱입니다.
큰 글씨, 간편 모드, 음성 입력으로 누구나 편하게 일상을 관리하세요.

✨ 주요 기능

📋 기본 기능
• 할 일 추가, 수정, 완료 표시
• 우선순위 설정 (높음/중간/낮음)
• 카테고리별 정리
• 스마트 검색 (제목, 설명, 카테고리)

⏰ 알림 & 반복
• 날짜/시간 알림 설정
• 반복 리마인더 (매일, 매주, 매월, 매년)
• 요일별 선택 가능
• 스누즈 기능 (5분, 10분, 30분, 1시간, 내일)

♿ 접근성 & 편의성
• TalkBack 스크린 리더 완벽 지원
• 4단계 글씨 크기 조절
• 간편 모드 (복잡한 기능 자동 숨김)
• 음성 입력 (할 일 제목 음성으로 입력)
• 음성 알림 (TTS로 할 일 내용 자동 읽기)
• 앱 내 도움말 및 FAQ

🏠 홈 화면 위젯
• 앱을 열지 않고도 할 일 확인
• 위젯에서 바로 완료 체크
• 자동 업데이트 및 다크 모드 지원
• 빠른 메모 위젯 (바로 할 일 추가)

📊 스마트 기능
• 위치 기반 리마인더 (특정 장소 도착 시 알림)
• 웹 링크 첨부 (관련 URL 저장)
• 자동 카테고리 제안 (AI 분석)
• 최적 시간 제안 (완료 패턴 분석)
• 완료 패턴 분석 (시간대/요일별 생산성)

📝 고급 기능
• 서브태스크 (할 일을 세부 항목으로 나누기)
• 이미지 첨부 (갤러리/카메라)
• 템플릿 시스템 (자주 사용하는 할 일 저장)
• 배치 작업 (여러 할 일 일괄 처리)
• 드래그 앤 드롭으로 순서 조정
• 태그 시스템 (콤마로 구분)

☁️ 동기화 & 데이터
• 로컬 저장 (오프라인 지원)
• Firebase 클라우드 동기화 (선택 사항)
• 백업/복원 (JSON 파일)
• 완료 이력 달력 (날짜별 기록)
• 통계 대시보드 (완료율, 진행 상황)

🎨 UI/UX
• Material Design 3
• 다크 모드 및 동적 컬러 (Android 12+)
• 부드러운 애니메이션
• 햅틱 피드백
• 고대비 모드

🔒 개인정보 보호
• 모든 데이터는 로컬에 저장
• Firebase 동기화는 선택 사항 (Google 로그인 필요)
• 광고 없음, 추적 없음

🏆 왜 '할 일 관리'인가?

✅ 70대도 쉽게 사용 - 큰 글씨, 한글 메뉴, 간편 모드
✅ TDD로 개발 - 안정성과 품질 검증
✅ 오프라인 우선 - 인터넷 없어도 완벽 작동
✅ 100% 무료 - 광고 없음, 인앱 구매 없음
✅ 오픈소스 - GitHub에서 코드 공개

📱 시스템 요구사항

• Android 8.0 (API 26) 이상
• 저장 공간: 약 20MB
• 선택 사항: Google 계정 (클라우드 동기화 시)

💡 사용 팁

1. 간편 모드 활성화 (설정 → 간편 모드)
2. 글씨 크기 조절 (설정 → 글씨 크기)
3. 음성 입력 사용 (할 일 추가 시 마이크 아이콘)
4. 위젯 추가 (홈 화면 길게 누르기)
5. 도움말 보기 (설정 → 도움말)

📧 문의 및 지원

버그 리포트, 기능 제안, 문의사항은 GitHub Issues로 남겨주세요.
GitHub: https://github.com/yourusername/reminder

🎉 버전 정보

현재 버전: v1.27.1
최종 업데이트: 2025-10-10

⭐ 앱이 마음에 드셨나요?
별점과 리뷰를 남겨주세요. 여러분의 피드백이 앱을 더 좋게 만듭니다!
```

### 카테고리

**주 카테고리**: 생산성 (Productivity)
**부 카테고리**: 도구 (Tools)

### 태그 (최대 5개)

1. 할 일
2. 리마인더
3. 메모
4. 생산성
5. 접근성

### 연락처 정보

- **이메일**: your.email@example.com
- **웹사이트**: https://github.com/yourusername/reminder
- **개인정보 처리방침**: https://yourusername.github.io/reminder/privacy-policy.html

## 스크린샷 가이드라인

### 요구사항

- **최소**: 2장 이상
- **권장**: 4~8장
- **형식**: PNG 또는 JPEG
- **크기**:
  - 휴대전화: 1080x1920 (9:16) ~ 1080x2340 (최소 320px, 최대 3840px)
  - 태블릿 (선택): 1200x1920, 1600x2560
- **파일 크기**: 최대 8MB

### 추천 스크린샷 구성

#### 1. 메인 화면 (HomeScreen)
- 리마인더 리스트 (3~5개 항목)
- 우선순위별 컬러 표시
- FAB 버튼 강조
- **캡션**: "할 일을 한눈에 확인하세요"

#### 2. 할 일 추가 화면 (AddEditReminderScreen)
- 제목, 설명 입력 폼
- 날짜/시간 선택
- 우선순위 선택
- 음성 입력 버튼 강조
- **캡션**: "음성으로 쉽게 추가"

#### 3. 위젯 화면
- 홈 화면에 위젯 표시
- 위젯에서 체크 가능한 모습
- **캡션**: "위젯으로 빠른 확인"

#### 4. 통계 화면 (StatisticsScreen)
- 완료율 그래프
- 카테고리별 통계
- **캡션**: "진행 상황을 시각화"

#### 5. 설정 화면 (SettingsScreen)
- 간편 모드, 글씨 크기 설정
- 다크 모드 토글
- **캡션**: "내 스타일로 커스터마이징"

#### 6. 패턴 분석 화면 (PatternAnalysisScreen)
- 시간대별 완료율
- 요일별 생산성
- **캡션**: "나만의 생산성 패턴 발견"

### 스크린샷 디자인 팁

1. **밝은 배경** 사용 (라이트 모드 선호)
2. **실제 데이터** 입력 (예: "장 보기", "운동하기", "보고서 작성")
3. **한글 텍스트** 명확히 보이도록
4. **상태바** 포함 (시간은 9:41 또는 10:00으로 통일)
5. **네비게이션 바** 포함
6. **디바이스 프레임** 추가 (권장)

### 아이콘

- **512x512px**: 고해상도 아이콘 (필수)
- **1024x1024px**: 기능 그래픽 (권장)
- **형식**: PNG (32비트, 알파 채널 포함)
- **투명 배경** 금지, 단색 배경 사용

## 개인정보 처리방침

### 필수 포함 사항

```markdown
# 개인정보 처리방침

**최종 업데이트**: 2025-10-10

할 일 관리("본 앱")는 사용자의 개인정보를 중요하게 생각합니다.

## 수집하는 정보

### 로컬 저장 데이터
본 앱은 다음 정보를 **사용자 기기에만 로컬로 저장**합니다:
- 할 일 제목 및 설명
- 날짜 및 시간 설정
- 우선순위 및 카테고리
- 서브태스크 및 이미지
- 앱 설정 (테마, 글씨 크기 등)

### Firebase 동기화 (선택 사항)
사용자가 클라우드 동기화를 활성화한 경우:
- Google 계정 정보 (이메일 주소)
- 위 로컬 데이터를 Firebase Firestore에 저장
- Firebase Authentication을 통한 인증

## 정보 사용 방식

- **앱 기능 제공**: 할 일 관리, 알림, 통계 등
- **데이터 동기화**: 사용자가 선택한 경우만 Firebase 사용
- **앱 개선**: 익명화된 크래시 리포트 (Firebase Crashlytics)

## 제3자 공유

본 앱은 사용자 데이터를 제3자와 **절대 공유하지 않습니다**.

단, Firebase 서비스 사용 시 Google의 개인정보 처리방침이 적용됩니다:
https://policies.google.com/privacy

## 데이터 보안

- 로컬 데이터: 기기에 암호화 저장 (Android Keystore)
- Firebase 데이터: SSL/TLS 암호화 전송
- 사용자 인증: Firebase Authentication 사용

## 사용자 권리

- **데이터 조회**: 앱 내에서 모든 데이터 확인 가능
- **데이터 수정**: 앱 내에서 직접 수정 가능
- **데이터 삭제**: 앱 삭제 시 모든 로컬 데이터 삭제
- **Firebase 데이터 삭제**: 설정 > 계정 > 데이터 삭제

## 어린이 개인정보

본 앱은 13세 미만 어린이를 대상으로 하지 않으며,
의도적으로 어린이의 개인정보를 수집하지 않습니다.

## 권한 사용

- **알림**: 리마인더 알림 전송
- **저장소**: 이미지 첨부 및 백업 파일 저장
- **위치** (선택): 위치 기반 리마인더 기능
- **마이크**: 음성 입력 기능
- **카메라**: 이미지 촬영 기능

모든 권한은 사용 시점에 요청하며, 거부 시에도 앱의 기본 기능 사용 가능합니다.

## 정책 변경

본 개인정보 처리방침은 법률 변경 또는 앱 업데이트에 따라 변경될 수 있습니다.
변경 시 앱 내 공지 및 이메일 안내를 제공합니다.

## 문의

개인정보 관련 문의사항은 아래로 연락주세요:
- 이메일: your.email@example.com
- GitHub: https://github.com/yourusername/reminder/issues

---

© 2025 할 일 관리. All rights reserved.
```

### 호스팅

개인정보 처리방침은 **공개 URL에 호스팅** 되어야 합니다:

**옵션 1: GitHub Pages**
1. `docs/privacy-policy.md` 생성
2. Settings > Pages > Source: main branch, /docs folder
3. URL: `https://yourusername.github.io/reminder/privacy-policy.html`

**옵션 2: Firebase Hosting**
```bash
firebase init hosting
firebase deploy --only hosting
```

## 릴리즈 빌드

### 1. 키스토어 생성

```bash
keytool -genkey -v -keystore reminder-release.keystore \
  -alias reminder \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000

# 정보 입력
# - 비밀번호: 안전한 비밀번호 (절대 Git에 커밋하지 말 것!)
# - 이름, 조직, 도시, 국가 등
```

**⚠️ 중요**: 키스토어 파일과 비밀번호를 **안전하게 백업**하세요.
분실 시 앱 업데이트 불가능!

### 2. key.properties 파일 생성

```properties
# key.properties (Git에 커밋하지 말 것!)
storePwd=<your-store-secret>
keyPwd=<your-key-secret>
keyAlias=reminder
storeFile=../reminder-release.keystore
```

### 3. build.gradle.kts 수정

```kotlin
// app/build.gradle.kts

// key.properties 로드
val keystorePropertiesFile = rootProject.file("key.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {
    // ...

    signingConfigs {
        create("release") {
            keyAlias = keystoreProperties["keyAlias"] as String?
            // key.properties에서 서명 정보 로드
            // keyPassword와 storePassword 속성 설정
            storeFile = keystoreProperties["storeFile"]?.let { file(it) }
            // 실제 코드에서는 keystoreProperties에서 값을 읽어옵니다
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

### 4. ProGuard 규칙 확인

```proguard
# app/proguard-rules.pro

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Firebase
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Kotlin
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }

# Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**
```

## APK/AAB 생성

### AAB (Android App Bundle) 생성 (권장)

```bash
# 릴리즈 AAB 빌드
./gradlew bundleRelease

# 출력 위치
# app/build/outputs/bundle/release/app-release.aab
```

### APK 생성 (직접 배포용)

```bash
# 릴리즈 APK 빌드
./gradlew assembleRelease

# 출력 위치
# app/build/outputs/apk/release/app-release.apk
```

### 빌드 검증

```bash
# APK 서명 확인
jarsigner -verify -verbose -certs app-release.apk

# APK 정보 확인
aapt dump badging app-release.apk

# AAB 정보 확인
bundletool build-apks --bundle=app-release.aab \
  --output=app-release.apks \
  --ks=reminder-release.keystore \
  --ks-key-alias=reminder
```

## 테스트 트랙

### 내부 테스트 (Internal Testing)

- **대상**: 최대 100명의 내부 테스터
- **용도**: 빠른 반복 테스트
- **검토**: 자동 승인 (수 분 내)

### 비공개 테스트 (Closed Testing)

- **대상**: 이메일 리스트 또는 Google 그룹
- **용도**: 베타 테스터 피드백 수집
- **검토**: 자동 승인 (수 시간 내)

### 공개 테스트 (Open Testing)

- **대상**: 누구나 참여 가능 (링크 공유)
- **용도**: 대규모 베타 테스트
- **검토**: 자동 승인 (수 시간 내)

### 프로덕션 (Production)

- **대상**: 모든 사용자
- **검토**: 수동 검토 (수 일 소요)

## 스토어 등록 절차

### 1. Google Play Console 가입

1. [Google Play Console](https://play.google.com/console) 접속
2. Google 계정 로그인
3. 개발자 등록비 $25 일회성 결제
4. 개발자 계정 정보 입력

### 2. 앱 생성

1. **앱 만들기** 클릭
2. 앱 이름: "할 일 관리"
3. 기본 언어: 한국어
4. 앱 또는 게임: 앱
5. 무료 또는 유료: 무료

### 3. 스토어 설정 입력

#### 앱 정보
- 앱 이름, 짧은 설명, 전체 설명
- 스크린샷 (최소 2장)
- 아이콘 (512x512px)
- 기능 그래픽 (1024x500px, 선택 사항)

#### 분류
- 카테고리: 생산성
- 태그: 할 일, 리마인더, 메모 등

#### 연락처
- 이메일, 웹사이트
- 개인정보 처리방침 URL (필수)

#### 콘텐츠 등급
- 설문조사 작성 (폭력, 성인 콘텐츠 등)
- 자동 등급 산정 (PEGI 3, ESRB Everyone 등)

### 4. 릴리즈 생성

#### 프로덕션 트랙 선택
1. **프로덕션** > **새 릴리즈 만들기**
2. AAB/APK 업로드
3. 릴리즈 이름: v1.27.1
4. 릴리즈 노트 입력:

```
v1.27.1 - 품질 개선

🧪 UI 테스트 확장
- 신규 기능 UI 테스트 33개 추가
- 테스트 커버리지 대폭 향상

🔧 에러 처리 강화
- 전역 예외 처리 개선
- 사용자 친화적 에러 메시지

♻️ 최신 API 적용
- Deprecated API 제거
- Compose 최신 API 사용

✅ 모든 테스트 통과
```

5. **검토** > **출시 시작**

### 5. 검토 대기

- **내부/비공개/공개 테스트**: 수 시간 내 승인
- **프로덕션**: 1~3일 검토 (최초 출시는 더 오래 걸릴 수 있음)

### 6. 출시 완료

- 승인 시 이메일 알림
- Play Store에서 앱 검색 가능
- 사용자 다운로드 시작

## 업데이트 출시

### 버전 코드 증가

```kotlin
// app/build.gradle.kts
android {
    defaultConfig {
        versionCode = 31  // 이전: 30
        versionName = "1.28.0"  // 이전: "1.27.1"
    }
}
```

### 릴리즈 노트 작성

```
v1.28.0 - 새로운 기능

✨ 신규 기능
- [기능 1 설명]
- [기능 2 설명]

🐛 버그 수정
- [버그 1 수정]
- [버그 2 수정]

⚡ 성능 개선
- [개선 사항]
```

### 단계적 출시 (Staged Rollout)

1. **프로덕션** > **새 릴리즈 만들기**
2. AAB 업로드
3. **출시 비율**: 10% → 50% → 100%
4. 크래시 모니터링
5. 문제 없으면 100% 확대

## 출시 후 모니터링

### 1. 크래시 모니터링

- **Firebase Crashlytics**: 실시간 크래시 리포트
- **Play Console > 품질 > Android 바이탈**: 크래시율, ANR률

### 2. 사용자 피드백

- **Play Console > 리뷰**: 사용자 별점 및 리뷰
- **평점 4.0 이상** 유지 목표
- 부정적 리뷰에 답변

### 3. 통계 확인

- **Play Console > 통계**:
  - 설치 수
  - 활성 사용자 수
  - 평균 평점
  - 국가별 분포

### 4. 업데이트 계획

- 주요 버그 수정: **긴급 업데이트** (핫픽스)
- 신규 기능: **월 1회 정기 업데이트**
- 마이너 개선: **분기별 업데이트**

## 유용한 링크

- [Google Play Console](https://play.google.com/console)
- [Play Store 정책](https://play.google.com/about/developer-content-policy/)
- [Android 개발자 가이드](https://developer.android.com/distribute)
- [Firebase Console](https://console.firebase.google.com/)
- [Material Design Icons](https://fonts.google.com/icons)

## 추가 팁

### ASO (App Store Optimization)

1. **키워드 최적화**: 제목과 설명에 검색 키워드 포함
   - "할 일", "리마인더", "메모", "TODO", "알림"
2. **고품질 스크린샷**: 첫 인상이 중요
3. **정기 업데이트**: 최신 상태 유지로 검색 순위 상승
4. **긍정적 리뷰**: 별점 4.5+ 목표

### A/B 테스트

Play Console의 **스토어 등록정보 실험** 기능:
- 아이콘 A/B 테스트
- 스크린샷 순서 테스트
- 설명 문구 테스트

### 프로모션

- **Play Store 배지**: 웹사이트, GitHub README에 추가
- **소셜 미디어**: 출시 공지
- **프로모션 코드**: 무료 인앱 구매 코드 생성 (해당 시)

---

**문서 버전**: v1.0
**최종 업데이트**: 2025-10-10
**작성자**: Claude Code

**🎉 출시 성공을 기원합니다!**
