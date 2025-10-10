# 🚀 다음 세션 시작 가이드

> **이 파일을 읽은 후 삭제하세요!**

## ✅ 이전 세션 완료 내역 (v1.28.0)

**날짜**: 2025-10-10
**버전**: v1.28.0 (versionCode 31)

### 완료된 작업
1. ✅ 주간/월간 트렌드 데이터 모델 설계 (Statistics.kt 기존 필드 활용)
2. ✅ StatisticsViewModel TDD 구현
   - Red: 테스트 2개 추가 (주간/월간 트렌드 계산)
   - Green: calculateDailyCompletions() 메서드 구현
3. ✅ TrendChart UI 컴포넌트 구현
   - MPAndroidChart LineChart를 Compose로 wrapping
   - 다크 모드 지원, Material 3 테마
4. ✅ StatisticsScreen에 차트 통합
   - WeeklyTrendCard (최근 7일)
   - MonthlyTrendCard (최근 30일)
5. ✅ 버전 업데이트 및 문서화
   - app/build.gradle.kts: versionCode 31, versionName "1.28.0"
   - CHANGELOG.md, CLAUDE.md 업데이트
6. ✅ Git 커밋 및 태그 생성
   - 3개 커밋: test/feat(viewmodel), feat(ui), chore(version)
   - 태그: v1.28.0

### Git 상태
```
현재 브랜치: main
origin/main보다 7개 커밋 앞섬
working tree clean ✓
```

---

## 📋 다음 작업 계획 (v1.29.0)

### 우선순위 1: 푸시 알림 고도화 🔔

**목표**: FCM 기반 리치 알림 시스템 구현

**작업 내용**:
1. **FCM 설정 및 통합**
   - Firebase Console에서 프로젝트 설정 확인
   - build.gradle.kts에 FCM 의존성 추가
   - FirebaseMessagingService 구현

2. **알림 채널 세분화** (Android 8.0+)
   - 우선순위별 채널 (높음/중간/낮음)
   - 카테고리별 채널
   - 사용자 설정에서 채널별 on/off

3. **리치 알림 구현**
   - 큰 이미지 표시 (BigPictureStyle)
   - 액션 버튼 ("완료", "1시간 후", "보기")
   - 인라인 답장 (빠른 메모 추가)

4. **TDD 구현**
   - NotificationManagerTest.kt
   - FcmServiceTest.kt

**예상 시간**: 2-3시간
**DB 변경**: 없음
**의존성 추가**: `firebase-messaging-ktx`

---

### 우선순위 2: 다국어 지원 (i18n) 🌍

**목표**: 한국어, 영어, 중국어 지원

**작업 내용**:
1. **리소스 분리**
   - `res/values/strings.xml` (기본: 한국어)
   - `res/values-en/strings.xml` (영어)
   - `res/values-zh/strings.xml` (중국어)

2. **동적 언어 변경**
   - SettingsScreen에 언어 선택 추가
   - DataStore에 선택 언어 저장
   - 앱 재시작 없이 언어 변경

3. **70대 사용자 배려**
   - 큰 토글 버튼 (한/영/中)
   - 간편 모드에서도 접근 가능

**예상 시간**: 3-4시간
**DB 변경**: 없음

---

### 우선순위 3: 커스텀 테마 🎨

**목표**: 사용자 정의 색상 테마

**작업 내용**:
1. **색상 선택기 UI**
   - 프리셋 테마 (파스텔, 비비드, 다크 등)
   - 커스텀 색상 선택 (Color Picker)

2. **테마 미리보기**
   - 실시간 미리보기
   - 적용 전 확인 가능

3. **DataStore 저장**
   - 선택한 색상 저장
   - 앱 시작 시 자동 적용

**예상 시간**: 2시간
**DB 변경**: 없음

---

## 🎯 빠른 시작 명령어

다음 세션 시작 시 아래 중 하나를 선택하세요:

```
"v1.29.0 FCM 알림 기능 구현해줘 (TDD로)"
```

```
"v1.29.0 다국어 지원 추가해줘"
```

```
"v1.29.0 커스텀 테마 기능 구현해줘"
```

또는 세 가지를 순차적으로 모두 구현:
```
"v1.29.0부터 v1.31.0까지 순차적으로 구현해줘"
- v1.29.0: FCM 알림
- v1.30.0: 다국어 지원
- v1.31.0: 커스텀 테마
```

---

## 📝 현재 프로젝트 상태

- **최신 버전**: v1.28.0 (versionCode 31)
- **DB 버전**: v12
- **주요 기능**: 28개 버전 릴리즈
- **테스트**: ViewModel, Repository, DAO, UI 테스트 완비
- **아키텍처**: MVVM, Clean Architecture
- **의존성**:
  - Jetpack Compose
  - Room Database
  - Firebase (Auth, Firestore, Crashlytics)
  - MPAndroidChart
  - Coil, WorkManager, DataStore

---

## 🔧 유지보수 작업 (언제든 가능)

- 코드 리팩토링
- 테스트 커버리지 확대
- 성능 최적화
- 문서 업데이트
- 미사용 코드 제거

---

**이 파일을 읽었으면 다음 명령어로 삭제하세요:**
```
"SESSION_START.md 읽었으니 삭제해줘"
```

**그리고 위 작업 중 하나를 선택해서 시작하세요! 🚀**
