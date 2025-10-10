# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.37.0] - 2025-10-10

### Added
- 🤖 **AI 스마트 추천 시스템** - 머신러닝 기반 지능형 리마인더 관리
  - **PriorityPredictor**: 제목/설명 분석 → 우선순위 자동 예측
    - 과거 패턴 학습 (키워드 기반)
    - 신뢰도 점수 계산 (사용 횟수, 최근성 고려)
    - 가중치 알고리즘 (usage × confidence × recency)
  - **DueDateSuggester**: 유사 작업 소요 시간 분석 → 마감일 제안
    - 카테고리별 평균 소요 일수 계산
    - 신뢰도 기반 제안 (데이터 분산도 고려)
  - **CategoryClassifier**: 자동 카테고리 분류 강화 (v1.25.0 ML 업그레이드)
    - 유사 텍스트 기반 카테고리 제안 (최대 3개)
    - 점수 계산 (사용 빈도 + 신뢰도 + 최근성)
  - **NotificationTimeSuggester**: 완료 시간대 패턴 분석 → 최적 알림 시간 제안
    - 요일별, 카테고리별 완료 시간 학습
    - 시간대별 생산성 분석
  - **SmartSuggestionChip**: AI 제안 UI 컴포넌트
    - 애니메이션 효과 (expandVertically + fadeIn)
    - 신뢰도 30% 이상만 표시
    - 클릭 시 자동 적용
- 💾 **ML 학습 데이터 테이블** (DB v15 → v16)
  - `ml_training_data` 테이블 생성
  - MLDataType enum (PRIORITY, CATEGORY, DUE_DATE, NOTIFICATION_TIME)
  - 학습 데이터 자동 저장 및 축적

### Changed
- 🗄️ **Database Migration**: v15 → v16
  - `ml_training_data` 테이블 추가 (10개 컬럼)
  - 3개 인덱스 추가 (dataType, dataType+inputText, createdAt)
- 🔄 **Converters 확장**
  - MLDataType enum 컨버터 추가
- 🔢 버전 업데이트: `versionCode = 40`, `versionName = "1.37.0"`

### Technical Details
- **Files Created**: 9개
  - `MLTrainingDataEntity.kt` (ML 학습 데이터 엔티티)
  - `MLTrainingDataDao.kt` (DAO, 12개 쿼리 메서드)
  - `PriorityPredictor.kt` (우선순위 예측기)
  - `DueDateSuggester.kt` (마감일 제안기)
  - `CategoryClassifier.kt` (카테고리 분류기)
  - `NotificationTimeSuggester.kt` (알림 시간 제안기)
  - `SmartSuggestionChip.kt` (AI 제안 UI)
  - `PriorityPredictorTest.kt` (TDD 테스트)
- **Files Modified**: 3개
  - `ReminderDatabase.kt` (엔티티 추가, MIGRATION_15_16)
  - `Converters.kt` (MLDataType 컨버터)
  - `build.gradle.kts` (버전), `CLAUDE.md`, `CHANGELOG.md`
- **TDD 방식 구현**: Red-Green-Refactor 사이클
  - Red: PriorityPredictorTest 5개 테스트 작성 (먼저 실패)
  - Green: PriorityPredictor 구현 (테스트 통과)
- **ML Algorithm**:
  - 유사도 기반 검색 (LIKE 쿼리)
  - 가중치 계산 (로그 스케일 사용 횟수 × 신뢰도 × 최근성)
  - 신뢰도 계산 (일치율 50% + 사용 신뢰도 25% + 평균 신뢰도 25%)
- **Lines Changed**: +1200 (approx.)

### Quality Improvements
- TDD 기반 안정적 구현
- 학습 데이터 자동 축적으로 사용할수록 정확도 향상
- 사용자 맞춤형 AI 제안 (개인화된 패턴 학습)
- UI 친화적 제안 (신뢰도 낮으면 숨김)

### Future Work (v1.37.1 예정)
- AddEditReminderScreen에 AI 제안 기능 통합
- 학습 데이터 정리 (90일 이상 사용 안 된 데이터 삭제)
- ML 모델 정확도 모니터링 (Firebase Analytics)

## [1.31.0] - 2025-10-10

### Added
- 🎨 **테마 프리셋 확장** - 4개에서 10개로 색상 테마 대폭 확대
  - **6개 신규 테마 추가**
    - 주황 (Orange): 따뜻하고 활기찬 색상
    - 빨강 (Red): 열정적이고 강렬한 색상
    - 청록 (Teal): 차분하고 세련된 색상
    - 호박색 (Amber): 밝고 경쾌한 색상
    - 남색 (Indigo): 깊이 있는 블루 계열
    - 갈색 (Brown): 따뜻하고 자연스러운 색상
  - **기존 테마 유지** (보라, 파랑, 초록, 핑크)
  - **라이트/다크 모드 모두 지원**
    - 각 테마별로 Light/Dark ColorScheme 구현
    - Material 3 디자인 가이드라인 준수
  - **색상 미리보기**
    - 테마 선택 UI에 색상 원형 미리보기 표시
    - Primary 색상을 24dp 원으로 시각화
    - 테두리 효과로 구분 강화

### Changed
- 🎨 **ThemePreset enum 확장**
  - 4개 → 10개 테마로 확장
  - 각 테마에 한글 레이블 추가 (보라, 파랑, 초록, 핑크, 주황, 빨강, 청록, 호박색, 남색, 갈색)
- 🎨 **Color.kt 대폭 확장**
  - 36개 신규 색상 상수 추가 (6개 테마 × 6개 색상)
  - 각 테마별 primary, secondary, tertiary 색상 정의
  - Light (40 계열), Dark (80 계열) 색상 쌍 구현
- 🎨 **Theme.kt ColorScheme 확장**
  - 12개 ColorScheme 추가 (6개 테마 × 2개 모드)
  - when() 표达식 업데이트로 모든 테마 지원
  - 동적 컬러 비활성화 시에만 프리셋 적용
- 🖼️ **SettingsScreen UI 개선**
  - ThemePresetSection 추가 (10개 테마 선택 가능)
  - 동적 컬러 비활성화 시에만 프리셋 섹션 표시
  - ThemePresetColorPreview 컴포넌트로 색상 미리보기
  - 라디오 버튼 기반 선택 UI
- 🔢 버전 업데이트: `versionCode = 34`, `versionName = "1.31.0"`

### Technical Details
- **Files Modified**: 4개
  - `app/build.gradle.kts` (버전)
  - `app/src/main/java/com/reminder/data/preferences/ThemePreset.kt` (6개 enum 추가)
  - `app/src/main/java/com/reminder/ui/theme/Color.kt` (+36개 색상)
  - `app/src/main/java/com/reminder/ui/theme/Theme.kt` (+12개 ColorScheme)
  - `app/src/main/java/com/reminder/ui/screen/SettingsScreen.kt` (UI 추가)
- **New Components**: 2개
  - `ThemePresetSection`: 테마 프리셋 선택 컴포넌트
  - `ThemePresetColorPreview`: 색상 미리보기 원형 컴포넌트
- **Helper Functions**: 1개
  - `getThemePresetLabel()`: ThemePreset → 한글 레이블 변환
- **Lines Changed**: +200 (approx.)
- **Color Palette Size**: 기존 24개 → 60개 색상 상수

### Quality Improvements
- 사용자 맞춤 경험 강화 (10가지 테마 선택)
- 시각적 피드백 개선 (색상 미리보기)
- Material 3 디자인 일관성 유지
- 동적 컬러와 프리셋 테마의 조화로운 통합

## [1.30.0] - 2025-10-10

### Added
- 🌐 **다국어 지원 (Internationalization)** - 한국어, 영어, 중국어 3개 언어 지원
  - `Language.kt` enum 추가 (SYSTEM, KOREAN, ENGLISH, CHINESE)
    - `toLocale()`: Language → Locale 변환
    - `fromCode()`: 언어 코드 → Language 변환
  - `LocaleHelper.kt` 유틸리티 클래스 구현
    - `updateLocale()`: Context에 언어 설정 적용
    - `getCurrentLocale()`: 현재 언어 가져오기
    - `getLanguageFromContext()`: Context → Language 변환
  - **리소스 파일 3개 생성**
    - `values/strings.xml`: 한국어 (165개 문자열)
    - `values-en/strings.xml`: English (165개 문자열)
    - `values-zh/strings.xml`: 中文 (165개 문자열)
  - **설정 화면에 언어 선택 UI 추가**
    - 라디오 버튼 방식 (시스템 기본값 / 한국어 / English / 中文)
    - 언어 변경 시 Activity 자동 재생성으로 즉시 반영
  - **SettingsViewModel 강화**
    - `updateLanguage()` 메서드 추가
    - Firebase Analytics 언어 변경 이벤트 로깅
  - **AnalyticsHelper 확장**
    - `logLanguageChanged()` 메서드 추가

### Changed
- 🏗️ **ReminderApplication 업데이트**
  - `attachBaseContext()` override로 앱 시작 시 저장된 언어 적용
  - Firebase Analytics에 언어 사용자 속성 추가
- 📱 **MainActivity 업데이트**
  - `attachBaseContext()` override로 Activity 언어 설정 적용
  - `LaunchedEffect`로 언어 변경 감지 및 자동 재생성
  - `currentLanguage` 필드로 언어 변경 추적
- 💾 **PreferencesRepository 확장**
  - `language` 필드 추가 (DataStore)
  - `updateLanguage()` 메서드 구현
  - UserPreferences에 `language: Language` 필드 추가
- 🔢 버전 업데이트: `versionCode = 33`, `versionName = "1.30.0"`

### Technical Details
- **Files Created**: 4개
  - `app/src/main/java/com/reminder/util/LocaleHelper.kt` (신규)
  - `app/src/main/res/values-en/strings.xml` (신규)
  - `app/src/main/res/values-zh/strings.xml` (신규)
  - `app/src/main/java/com/reminder/data/preferences/Language.kt` (UserPreferences.kt에 포함)
- **Files Modified**: 8개
  - `app/build.gradle.kts` (버전)
  - `app/src/main/java/com/reminder/data/preferences/UserPreferences.kt` (Language enum 추가)
  - `app/src/main/java/com/reminder/data/preferences/PreferencesRepository.kt` (language 필드)
  - `app/src/main/java/com/reminder/viewmodel/SettingsViewModel.kt` (updateLanguage)
  - `app/src/main/java/com/reminder/analytics/AnalyticsHelper.kt` (logLanguageChanged)
  - `app/src/main/java/com/reminder/ui/screen/SettingsScreen.kt` (언어 UI)
  - `app/src/main/java/com/reminder/ReminderApplication.kt` (attachBaseContext)
  - `app/src/main/java/com/reminder/MainActivity.kt` (언어 변경 리스너)
  - `app/src/main/res/values/strings.xml` (기존 하드코딩 문자열 리소스화)
- **Lines Changed**: +800 (approx.)
- **Supported Languages**: 3개 (Korean, English, Simplified Chinese)

### Quality Improvements
- 국제화를 통한 글로벌 사용자 접근성 향상
- 앱 재시작 없이 언어 변경 즉시 적용
- 체계적인 리소스 관리 (하드코딩 제거)
- Firebase Analytics로 언어 사용 패턴 추적

## [1.29.0] - 2025-10-10

### Added
- 🔔 **FCM 푸시 알림 고도화** - Firebase Cloud Messaging 기반 리치 알림 시스템
  - `ReminderMessagingService.kt` 신규 구현
    - FCM 메시지 수신 및 처리
    - 데이터 메시지 파싱 (reminderId, title, description, priority, imageUri 등)
    - 알림 메시지 fallback 지원
    - 새로운 FCM 토큰 수신 처리
  - `ReminderNotificationChannel.kt` 신규 enum 추가
    - 우선순위별 3개 채널 정의 (HIGH, MEDIUM, LOW)
    - 채널별 중요도 설정 (IMPORTANCE_HIGH/DEFAULT/LOW)
    - Priority enum 자동 변환 지원
  - **알림 채널 세분화** (Android 8.0+)
    - 높은 우선순위: 알림음 + 진동 + 헤드업 알림
    - 중간 우선순위: 알림음만
    - 낮은 우선순위: 소리/진동 없음 (상태바만)
  - **리치 알림 (BigPictureStyle)**
    - `buildRichNotification()` 메서드 추가
    - 이미지 첨부 시 큰 이미지 알림 표시
    - URI로부터 Bitmap 자동 로드
  - **액션 버튼 알림**
    - `buildNotificationWithActions()` 메서드 추가
    - "완료" 버튼 (완료 처리)
    - "1시간 후" 버튼 (스누즈)
    - PendingIntent 기반 액션 처리

### Changed
- 🔧 **NotificationHelper 강화**
  - `createAllNotificationChannels()` 메서드 추가
    - 3개 채널을 한 번에 생성
    - 사용자 설정(소리/진동/LED) 자동 반영
    - 낮은 우선순위는 소리/진동 비활성화
  - `buildNotification()` 메서드 수정
    - 우선순위별로 다른 채널 ID 자동 선택
    - ReminderNotificationChannel.fromPriority() 활용
  - Helper 메서드 추가
    - `createActionPendingIntent()`: 액션 버튼용 PendingIntent
    - `loadBitmapFromUri()`: URI→Bitmap 변환
- 🏗️ **ReminderApplication 업데이트**
  - `createAllNotificationChannels()` 호출로 변경 (기존 단일 채널에서 확장)
- 📱 **AndroidManifest 업데이트**
  - ReminderMessagingService 등록
  - FCM intent-filter 추가 (com.google.firebase.MESSAGING_EVENT)
- 🔢 버전 업데이트: `versionCode = 32`, `versionName = "1.29.0"`

### Technical Details
- **Files Modified**: 6개
  - `app/build.gradle.kts` (FCM 의존성, 버전)
  - `app/src/main/java/com/reminder/notification/ReminderNotificationChannel.kt` (신규)
  - `app/src/main/java/com/reminder/fcm/ReminderMessagingService.kt` (신규)
  - `app/src/main/java/com/reminder/notification/NotificationHelper.kt` (확장)
  - `app/src/main/java/com/reminder/ReminderApplication.kt` (채널 초기화)
  - `app/src/main/AndroidManifest.xml` (서비스 등록)
  - `app/src/androidTest/java/com/reminder/notification/NotificationHelperTest.kt` (테스트 추가)
- **TDD 방식 구현**: Red-Green-Refactor 사이클
  - Red: 5개 새 테스트 작성 (세분화 채널, 리치 알림, 액션 버튼)
  - Green: NotificationHelper 메서드 3개 구현
- **Dependencies**: firebase-messaging-ktx (BOM 33.7.0)
- **Lines Changed**: +350 (approx.)

### Quality Improvements
- TDD 기반 안정적 구현
- 우선순위별 알림 체계화
- FCM 통합으로 원격 알림 가능
- 사용자 경험 개선 (이미지, 액션 버튼)

## [1.28.0] - 2025-10-10

### Added
- 📊 **통계 차트 시각화 강화** - 주간/월간 완료 트렌드 그래프 추가
  - `TrendChart.kt` 신규 컴포넌트 작성
    - MPAndroidChart LineChart를 Compose로 wrapping
    - 부드러운 곡선 라인 차트 (Cubic Bezier)
    - 다크 모드 자동 지원
    - 애니메이션 효과 및 터치 인터랙션
    - Material 3 테마 색상 적용
  - `WeeklyTrendCard`: 최근 7일 완료 트렌드 시각화
    - 일별 완료 개수를 라인 차트로 표시
    - "오늘", "1일 전", ..., "6일 전" 라벨
  - `MonthlyTrendCard`: 최근 30일 완료 트렌드 시각화
    - 5일 간격으로 날짜 라벨 표시
    - 장기 트렌드 패턴 파악 가능
  - 데이터 없을 때 안내 메시지 자동 표시

### Changed
- 🧠 **StatisticsViewModel 강화**
  - `calculateDailyCompletions()` 메서드 추가
    - 최근 N일간 일별 완료 개수 자동 계산
    - `updatedAt` 기준으로 날짜 차이 계산 (ChronoUnit.DAYS)
    - 범위 내 리마인더만 카운트
  - `weeklyCompleted`, `monthlyCompleted` 필드 자동 계산 및 반환
- 🔢 버전 업데이트: `versionCode = 31`, `versionName = "1.28.0"`

### Technical Details
- **Files Modified**: 4개
  - `app/build.gradle.kts` (버전)
  - `app/src/main/java/com/reminder/viewmodel/StatisticsViewModel.kt` (트렌드 계산)
  - `app/src/main/java/com/reminder/ui/components/TrendChart.kt` (신규)
  - `app/src/main/java/com/reminder/ui/screen/StatisticsScreen.kt` (차트 통합)
  - `app/src/test/java/com/reminder/viewmodel/StatisticsViewModelTest.kt` (테스트 추가)
- **TDD 방식 구현**: Red-Green-Refactor 사이클 완수
  - Red: 주간/월간 트렌드 테스트 2개 추가
  - Green: calculateDailyCompletions() 구현
- **Chart Library**: MPAndroidChart v3.1.0
- **Lines Changed**: +310

### Quality Improvements
- TDD 기반 안정적 구현
- 시각화를 통한 사용자 경험 개선
- 장기 트렌드 분석 가능

## [1.27.1] - 2025-10-10

### Added
- 🧪 **UI 테스트 확장** - v1.22.0~v1.26.0 신규 기능 UI 테스트 추가
  - `PatternAnalysisScreenTest.kt` 신규 작성 (14개 테스트)
    - 화면 제목, 뒤로가기, 로딩 상태 테스트
    - 요약 카드, 완료율, 생산적인 시간대 테스트
    - 시간대별/요일별 완료율 테스트
    - 평균 완료 시간 테스트 (시간/분 표시 분기)
  - `AddEditReminderScreenTest.kt` 확장 (19개 테스트 추가)
    - 위치 기반 알림 UI 테스트 (v1.22): 섹션 표시, 필드 입력, 기존 데이터 표시
    - 웹 링크 첨부 UI 테스트 (v1.23): 필드 표시, 입력, 기존 데이터 표시
    - TTS 자동 읽기 UI 테스트 (v1.24): 토글 표시, 동작, 기존 설정 표시
    - 카테고리 자동 제안 UI 테스트 (v1.25): 칩 표시, 클릭 시 카테고리 설정
    - 최적 시간 제안 UI 테스트 (v1.26): 날짜 선택 시 추천 시간 표시
    - 음성 입력 버튼 테스트: 마이크 아이콘 표시 확인
    - 간편 모드 테스트 (5개): 카테고리/위치/웹링크/TTS/반복 설정 숨김 검증

### Changed
- 🔧 **에러 처리 강화**
  - 전역 CoroutineExceptionHandler 추가 (ReminderApplication)
    - 모든 코루틴 예외 자동 캐치 및 로깅
    - Firebase Crashlytics 자동 연동
    - 컨텍스트 정보 함께 기록
  - 네트워크 에러 메시지 개선 (FirestoreDataSource)
    - FirebaseNetworkException → "네트워크 연결을 확인해주세요"
    - PERMISSION_DENIED → "다시 로그인해주세요"
    - UNAVAILABLE → "잠시 후 다시 시도해주세요"
    - DEADLINE_EXCEEDED → "네트워크 연결을 확인해주세요"
  - 사용자 친화적인 예외 메시지 생성 함수 추가

- ♻️ **Deprecated API 제거 및 최신 API 교체**
  - `Icons.Default.ArrowBack` → `Icons.AutoMirrored.Filled.ArrowBack` (6개 파일)
    - PatternAnalysisScreen, StatisticsScreen, AddEditReminderScreen
    - CompletionHistoryScreen, HelpScreen, SettingsScreen
  - `Divider()` → `HorizontalDivider()` (전체 프로젝트)
  - `LinearProgressIndicator(progress: Float)` → `LinearProgressIndicator(progress: () -> Float)` (3개 위치)
    - PatternAnalysisScreen 완료율 및 진행률 표시 (람다 버전 사용)

- 🔢 버전 업데이트: `versionCode = 30`, `versionName = "1.27.1"`

### Fixed
- 🐛 Compose deprecated API 경고 제거 (주요 경고만 수정, 일부 경고는 복잡도로 인해 보류)

### Technical Details
- **Files Modified**: 12개
  - `app/build.gradle.kts` (버전)
  - `app/src/main/java/com/reminder/ReminderApplication.kt` (CoroutineExceptionHandler)
  - `app/src/main/java/com/reminder/data/remote/FirestoreDataSource.kt` (에러 처리)
  - `app/src/androidTest/java/com/reminder/ui/screen/PatternAnalysisScreenTest.kt` (신규)
  - `app/src/androidTest/java/com/reminder/ui/screen/AddEditReminderScreenTest.kt` (확장)
  - UI 화면 6개 (Deprecated API 교체)
- **Tests**: 모든 유닛 테스트 통과 ✅
- **Build**: 성공 (BUILD SUCCESSFUL)
- **Lines Changed**: +917 -42 (12 files)

### Quality Improvements
- 테스트 커버리지 대폭 향상 (UI 테스트 33개 추가)
- 에러 핸들링 안정성 향상
- 코드 품질 개선 (최신 API 사용)

## [1.27.0] - 2025-10-09

### Added
- 🎨 **UI 통합 업데이트** - v1.22.0~v1.26.0 백엔드 기능들의 UI 구현 완료
  - 📍 위치 입력 UI (위도, 경도, 위치 이름, 반경 미터)
  - 🔗 웹 링크 입력 필드 with 자동 https:// placeholder
  - 🔊 TTS 자동 읽기 토글 스위치
  - 🏷️ 카테고리 자동 제안 칩 (LazyRow)
  - ⏰ 최적 시간 제안 버튼 (날짜 선택 시 자동 표시)
  - 📊 완료 패턴 분석 대시보드 화면 (`PatternAnalysisScreen.kt`)
    - 전체 완료율 시각화
    - 시간대별 완료율 (상위 5개)
    - 요일별 완료율 (전체)
    - 평균 완료 소요 시간
- 🔗 **네비게이션 통합**
  - PatternAnalysisScreen 라우트 추가 (`pattern_analysis`)
  - StatisticsScreen에서 패턴 분석 버튼 연결
- ✨ **ReminderCard 업데이트**
  - 위치 이름 표시 (📍 아이콘)
  - 웹 링크 표시 (🔗 아이콘, 1줄 말줄임)
  - TTS 활성화 표시 (🔊 아이콘)

### Changed
- 🔧 버전 업데이트: `versionCode = 29`, `versionName = "1.27.0"`
- 📝 CLAUDE.md 업데이트: v1.27.0 현황 반영
- 📄 README.md 업데이트: 스마트 기능 섹션 추가, v1.15.0~v1.27.0 릴리즈 노트 추가

### Fixed
- 🐛 PatternAnalysisScreen.kt smart cast 오류 수정 (pattern → currentPattern 지역 변수 사용)

### Technical Details
- **Files Modified**: 7개
  - `app/build.gradle.kts` (버전)
  - `app/src/main/java/com/reminder/ui/screen/AddEditReminderScreen.kt` (UI 추가)
  - `app/src/main/java/com/reminder/ui/screen/PatternAnalysisScreen.kt` (신규)
  - `app/src/main/java/com/reminder/ui/screen/StatisticsScreen.kt` (버튼 추가)
  - `app/src/main/java/com/reminder/ui/components/ReminderCard.kt` (필드 표시)
  - `app/src/main/java/com/reminder/MainActivity.kt` (네비게이션)
  - `CLAUDE.md`, `README.md`, `CHANGELOG.md` (문서)
- **Tests**: 115개 모두 통과 ✅
- **Build**: 성공 (1m 46s)

## [1.22.0 ~ 1.26.0] - 2025-10-09

### v1.26.0 - 완료 패턴 분석
- 📊 CompletionPatternAnalyzer 구현
- 시간대별/요일별 생산성 분석
- 최적 시간 제안 기능

### v1.25.0 - 자동 카테고리 제안
- 🏷️ CategorySuggestionHelper 구현
- 키워드/패턴/빈도 기반 제안

### v1.24.0 - 음성 알림 (TTS)
- 🔊 TtsHelper 구현
- 한국어 음성 지원
- readAloud 필드 추가 (DB v11→v12)

### v1.23.0 - 웹 링크 첨부
- 🔗 UrlValidator 구현
- 자동 https:// 정규화
- webLink 필드 추가 (DB v10→v11)

### v1.22.0 - 위치 기반 리마인더
- 📍 LocationManager 구현
- Google Play Services Location
- location* 필드 추가 (DB v9→v10)

## [1.19.0 ~ 1.21.0] - 2025-10-09
- 🔵 배지 카운트 (v1.19.0)
- ⏰ 스누즈 기능 (v1.20.0)
- 📝 빠른 메모 위젯 (v1.21.0)

## [1.15.0 ~ 1.18.1] - 2025-10-09
- 🎨 UX/UI 개선 (햅틱 피드백, 고대비 모드)
- 🎬 온보딩 애니메이션

## [1.8.0] - 2025-10-09

### Added
- **접근성 개선**: TalkBack 스크린 리더 완벽 지원
  - 모든 UI 요소에 한글 contentDescription 추가
  - 우선순위, 완료 상태, 버튼 등 명확한 음성 안내
- **글씨 크기 조절**: 4단계 글씨 크기 설정
  - 작게 (0.85배), 보통 (1.0배), 크게 (1.15배), 아주 크게 (1.3배)
  - 전체 앱 Typography 동적 스케일링
- **간편 모드**: 70세+ 사용자를 위한 단순화 인터페이스
  - 복잡한 기능 숨김 (필터, 정렬, 통계, 검색)
  - 더 큰 버튼 (FAB 72dp, 아이콘 36dp)
  - 카테고리, 반복 설정 등 고급 기능 자동 숨김
- **도움말 화면**: 앱 내 종합 사용 가이드
  - 주요 기능 설명 (8개 항목)
  - 설정 기능 안내 (3개 항목)
  - 자주 묻는 질문 FAQ (5개 항목, 펼치기/접기 가능)
- **음성 입력**: 할 일 제목 음성으로 입력
  - Android Speech Recognition API 통합
  - 한국어 음성 인식 (ko-KR)
  - 큰 마이크 버튼 (56dp) - 노년층 사용자 배려
  - RECORD_AUDIO 권한 동적 요청

### Changed
- **설정 화면**: 간편 모드 시 동적 컬러 옵션 자동 숨김
- **홈 화면**: 간편 모드 시 FAB 크기 1.6배 확대
- **할 일 추가 화면**: 간편 모드 시 카테고리/반복 섹션 숨김

### Features
- **동적 Typography**: 사용자 설정에 따른 실시간 글씨 크기 변경
- **조건부 UI**: simpleMode 파라미터 기반 UI 요소 동적 표시/숨김
- **FAQ 컴포넌트**: 클릭으로 펼치기/접기 가능한 대화형 FAQ 카드
- **음성 권한 처리**: ActivityResultContracts로 권한 요청 및 처리

### Technical Details
- TDD 방식 구현 (AccessibilityTest.kt 작성)
- DataStore에 fontSize, simpleMode 필드 추가
- getTypography() 함수로 동적 Typography 생성
- Speech Recognition launcher 패턴 구현
- @OptIn(ExperimentalMaterial3Api::class) for Card onClick
- 모든 Screen에 simpleMode 파라미터 전파

### Accessibility
- 체크박스: "완료 여부 체크박스"
- 우선순위: "우선순위: 높음/중간/낮음"
- 삭제 버튼: "할 일 삭제"
- 통계 차트: 상세한 음성 설명 (예: "완료율 75 퍼센트")

## [1.7.0] - 2025-10-08

### Added
- **홈 화면 위젯**: 홈 화면에서 할 일 목록 바로 확인
- **위젯 완료 체크**: 위젯에서 직접 할 일 완료 처리 가능
- **다크 모드 위젯**: 시스템 다크 모드 자동 대응
- **실시간 자동 업데이트**: 할 일 추가/수정/삭제 시 위젯 자동 갱신

### Changed
- **앱 이름**: "Reminder" → "할 일 관리" (70대 사용자 친화)
- **위젯 텍스트**: 모든 영어 표시를 한글로 변경

### Features
- **위젯 정렬**: 마감일 가까운 순서로 자동 정렬 (최대 10개 표시)
- **우선순위 표시**: 좌측 색상 바로 우선순위 구분
- **새로고침 버튼**: 수동 위젯 업데이트 지원
- **앱 바로가기**: 위젯 항목 클릭 시 앱 열기

### Technical Details
- TDD 방식 구현 (테스트 우선 작성)
- RemoteViewsService/Factory 패턴
- AppWidgetProvider 커스텀 구현
- Repository 변경 자동 감지 및 위젯 업데이트

## [1.0.0] - 2025-10-07

### Added
- 기본 MVVM 아키텍처 구현
- Room Database를 사용한 로컬 데이터 저장
- 리마인더 CRUD 기능 (생성, 조회, 수정, 삭제)
- 리마인더 완료/미완료 토글 기능
- 우선순위 시스템 (낮음, 중간, 높음)
- 카테고리 분류 기능
- 검색 기능 (제목, 설명, 카테고리 기반)
- Material 3 디자인 시스템 적용
- 동적 컬러 지원 (Android 12+)
- ViewModel 단위 테스트 (100% 커버리지)
- Repository 단위 테스트 (100% 커버리지)
- DAO 통합 테스트 (주요 쿼리 100% 커버리지)

### Features
- **홈 화면**: 활성 리마인더 목록 표시
- **추가/편집 화면**: 리마인더 생성 및 수정
- **검색**: 실시간 검색 기능
- **정렬**: 마감일 및 우선순위 기반 자동 정렬
- **우선순위 표시**: 컬러 인디케이터로 시각적 구분

### Technical Details
- Kotlin 1.9.20
- Jetpack Compose
- Room Database 2.6.1
- MVVM Architecture
- Coroutines & Flow
- Navigation Compose
- Min SDK 26 (Android 8.0)
- Target SDK 34 (Android 14)

[1.8.0]: https://github.com/yourusername/reminder/releases/tag/v1.8.0
[1.7.0]: https://github.com/yourusername/reminder/releases/tag/v1.7.0
[1.0.0]: https://github.com/yourusername/reminder/releases/tag/v1.0.0
