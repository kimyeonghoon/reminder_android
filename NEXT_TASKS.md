# 다음 작업 계획

> 마지막 업데이트: 2025-10-10 (v1.28.0 완료)
> **📌 다음 세션 시작 시 SESSION_START.md를 먼저 읽으세요!**

## 📋 우선순위별 작업 목록

### ✅ 완료된 작업

#### ~~1. Git 상태 정리 및 커밋~~ ✓
- v1.27.1에서 완료

#### ~~2. Firebase 완료 항목 삭제 로직 구현~~ ✓
- v1.27.1에서 완료 (FirebaseSyncRepository.kt:99 TODO 해결)

#### ~~3. v1.28.0 차트 시각화 강화~~ ✓
- TrendChart 컴포넌트 구현
- StatisticsViewModel 트렌드 계산
- 주간/월간 완료 그래프 추가

---

### 🔥 다음 버전 계획 (v1.29.0 ~ v1.31.0)

#### v1.29.0: 푸시 알림 고도화 🔔
**목표**: FCM 기반 리치 알림 시스템

**주요 기능**:
- Firebase Cloud Messaging 통합
- 알림 채널 세분화 (우선순위별, 카테고리별)
- 리치 알림 (BigPictureStyle, 액션 버튼)
- 인라인 답장 (빠른 메모 추가)

**기술 스택**:
- `firebase-messaging-ktx`
- NotificationCompat.Builder
- NotificationChannel (Android 8.0+)

**예상 시간**: 2-3시간
**DB 변경**: 없음

---

#### v1.30.0: 다국어 지원 (i18n) 🌍
**목표**: 한국어, 영어, 중국어 지원

**주요 기능**:
- strings.xml 리소스 분리 (ko, en, zh)
- 동적 언어 변경 (앱 재시작 불필요)
- 설정 화면에 언어 선택 UI
- 70대 사용자 배려 (큰 토글 버튼)

**기술 스택**:
- Android Localization
- DataStore (언어 설정 저장)
- Configuration.setLocale()

**예상 시간**: 3-4시간
**DB 변경**: 없음

---

#### v1.31.0: 커스텀 테마 🎨
**목표**: 사용자 정의 색상 테마

**주요 기능**:
- 프리셋 테마 (파스텔, 비비드, 다크 등)
- 커스텀 색상 선택 (Color Picker)
- 실시간 테마 미리보기
- DataStore에 테마 저장

**기술 스택**:
- Material 3 Dynamic Color
- ColorScheme 커스터마이징
- DataStore

**예상 시간**: 2시간
**DB 변경**: 없음

---

### 🔵 유지보수 및 개선

#### UI 테스트 커버리지
- ✅ `StatisticsScreenTest.kt` (v1.27.1 완료)
- ✅ `CompletionHistoryScreenTest.kt` (v1.27.1 완료)
- ✅ `SettingsScreenTest.kt` (v1.27.1 완료)
- ✅ `HelpScreenTest.kt` (v1.27.1 완료)

#### 문서
- ✅ CLAUDE.md 업데이트 (v1.28.0)
- ✅ CHANGELOG.md 업데이트 (v1.28.0)
- [ ] README.md에 최신 기능 스크린샷 추가
- [ ] API 문서 생성 (KDoc → Dokka)

---

## 🚀 빠른 시작 가이드

### ⚠️ 다음 세션 시작 시 필수!

**먼저 이 명령어를 실행하세요:**
```
"SESSION_START.md 읽어줘"
```

그 다음 작업 선택:

1. **v1.29.0 FCM 알림 구현**:
   ```
   "v1.29.0 FCM 알림 기능 구현해줘 (TDD로)"
   ```

2. **v1.30.0 다국어 지원**:
   ```
   "v1.30.0 다국어 지원 추가해줘"
   ```

3. **v1.31.0 커스텀 테마**:
   ```
   "v1.31.0 커스텀 테마 기능 구현해줘"
   ```

4. **모두 순차적으로**:
   ```
   "v1.29.0부터 v1.31.0까지 순차적으로 구현해줘"
   ```

---

## 📝 참고사항

- **TDD 필수**: 모든 새 기능은 테스트 먼저 작성
- **커밋 메시지**: `type(scope): 한글 제목` 형식
- **버전 업데이트**: `app/build.gradle.kts`에서 versionCode/versionName 수정
- **문서화**: CHANGELOG.md, CLAUDE.md 동기화

---

## 🔗 관련 파일

- **세션 시작**: `SESSION_START.md` ⭐ (먼저 읽기)
- **프로젝트 가이드**: `CLAUDE.md`
- **변경 이력**: `CHANGELOG.md`
- **빌드 설정**: `app/build.gradle.kts`

---

## 📊 현재 프로젝트 현황

- **최신 버전**: v1.28.0 (versionCode 31)
- **DB 버전**: v12
- **총 릴리즈**: 28개 버전
- **주요 기능**:
  - ✅ CRUD, 알림, Firebase 동기화
  - ✅ 위젯, 접근성, 음성 입력
  - ✅ 위치 기반 알림, TTS, 패턴 분석
  - ✅ 통계 차트 시각화 (v1.28.0 NEW!)
  - 🔜 FCM 알림 (v1.29.0 계획)
  - 🔜 다국어 지원 (v1.30.0 계획)
  - 🔜 커스텀 테마 (v1.31.0 계획)

---

**다음 세션 시작 시: SESSION_START.md를 먼저 읽으세요! 📌**
