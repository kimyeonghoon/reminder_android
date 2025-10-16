# 다음 작업 계획

> 마지막 업데이트: 2025-10-16 (v1.68.3 완료)
> **📌 다음 세션 시작 시 CLAUDE.md를 먼저 읽으세요!**

---

## 📊 현재 프로젝트 현황

- **최신 버전**: v1.68.3 (versionCode 75, DB v27)
- **DB 버전**: v27
- **총 릴리즈**: 79개 버전
- **테스트 커버리지**: 326/326 통과 (100% ✅)

### ✅ 완료된 주요 기능
- ✅ CRUD, 알림, Firebase 실시간 동기화
- ✅ 홈 화면 위젯, 빠른 메모 위젯
- ✅ 접근성, 음성 입력, 간편 모드
- ✅ 서브태스크, 이미지 첨부, 백업/복원
- ✅ 템플릿 시스템, 배치 작업, 태그 시스템
- ✅ 배지 카운트, 스누즈 기능
- ✅ 위치 기반 리마인더 (Geofencing)
- ✅ 웹 링크 첨부, 음성 알림 (TTS)
- ✅ 자동 카테고리 제안, 완료 패턴 분석
- ✅ 통계 차트 시각화, FCM 푸시 알림
- ✅ 다국어 지원 (한/영/중), 테마 프리셋
- ✅ 고급 검색/필터링, 저장된 필터
- ✅ 목표 설정 시스템, 생산성 인사이트
- ✅ 위젯 2.0 (설정, 주기적 업데이트)
- ✅ 반복 작업 고급 옵션 (일/주/월/년)
- ✅ 협업 기능 (권한 관리, 공유 리스트)
- ✅ AI 스마트 추천 (ML 기반 우선순위/카테고리 예측)
- ✅ 오프라인 모드 강화, 첨부파일 시스템 고도화
- ✅ 캘린더 통합 (CalendarContract API, 양방향 동기화)
- ✅ Quick Settings Tile (알림창 빠른 추가)
- ✅ Archive System (완료 리마인더 자동 아카이브)
- ✅ Habit Tracker (습관 추적 및 Streak 관리)
- ✅ Pomodoro Timer (25/5/15분 집중/휴식 타이머)
- ✅ 테스트 안정화 (v1.45.1: Fake 구현 패턴, 213개 테스트 100% 통과)
- ✅ Bottom Navigation Bar (v1.46.0~v1.46.2: 5개 메인 탭, UI 정리 완료)
- ✅ **Eisenhower Matrix (v1.47.0: 중요도×긴급도 매트릭스, TDD)** 🎯
- ✅ **AI 긴급도 예측 (v1.48.0: 키워드 기반 NLP 분석, 자동 제안)** 🤖
- ✅ **Eisenhower Matrix 고도화 2/3단계 (v1.49.0~v1.50.0: 통계, 이동, 트렌드)** 📊
- ✅ **포커스 모드 (v1.51.0: 집중 타이머, DO_FIRST 연동, 세션 관리, TDD)** 🎯
- ✅ **포커스 모드 Bottom Navigation 통합 (v1.52.0: 1탭 접근, Settings 이동)** 🎯
- ✅ **Eisenhower Matrix Long Press 이동 (v1.53.0: 빠른 이동 UX 개선)** 🎯
- ✅ **방해 금지 모드 (v1.54.0: DND 자동 활성화, 세션 연동, API 23+)** 🔕
- ✅ **성능 최적화 (v1.55.0: Room 인덱스, 앱 시작 15-20ms 개선)** ⚡
- ✅ **UI 일관성 검토 (v1.56.0: Material 3, 8dp 그리드 99% 준수)** 🎨
- ✅ **접근성 검토 (v1.57.0: WCAG AA 100% 준수, TalkBack 완벽 지원)** ♿
- ✅ **온보딩 개선 (v1.58.0: 6개 페이지, 한국어, 신규 기능 소개)** 🎓
- ✅ **코드 품질 개선 (v1.59.0~v1.62.0: Lint 경고 수정, 성능 최적화, RemoteViews 호환)** 🧹
- ✅ **Bottom Navigation 통합 (v1.63.0: TopAppBar 아키텍처 개선, UI 정리)** 🎨
- ✅ **테스트 함수명 표준화 (v1.63.1: 38개 파일, 370+ 함수 영어 변환, UI 테스트 강화)** ✅
- ✅ **RecurrencePattern 레거시 제거 (v1.64.0: 새 RecurrenceRule 시스템으로 완전 전환)** 🔄
- ✅ **RecurrenceRule UI 재구현 (v1.65.0: RecurrenceSelector 복원, 미리 알림 기능)** 🔄
- ✅ **hasTime 필드 추가 (v1.66.0: 날짜/시간 구분 명시화, 00:00 문제 해결)** 🕒
- ✅ **카카오 로컬 API 장소 검색 (v1.67.0: 실시간 자동완성, 하이브리드 입력, TDD)** 📍
- ✅ **위젯 데이터 표시 버그 수정 (v1.67.1: Repository 싱글톤 사용, 배터리 제약 제거)** 🐛
- ✅ **카카오맵 SDK 통합 (v1.68.0: 장소 검색 결과 지도 시각화, 위치 선택 UI, TDD 완료)** 🗺️
- ✅ **코드 리팩토링 (v1.68.2: MainActivity, AddEditReminderScreen 550줄 감소, 재사용 컴포넌트 4개 생성)** 🧹
- ✅ **대규모 리팩토링 (v1.68.3: ReminderViewModel, EisenhowerMatrixScreen, FocusModeScreen 총 1,396줄 감소)** 🧹✨

---

## 🔥 최근 완료: v1.68.3 대규모 리팩토링 ✅ 🧹✨

**완료일**: 2025-10-16
**목적**: God Class 해체 및 컴포넌트 분리로 유지보수성 극대화

### 성과 요약
- **ReminderViewModel.kt**: 678줄 → 257줄 (-421줄, -62.1%) - Facade Pattern 적용
- **EisenhowerMatrixScreen.kt**: 671줄 → 184줄 (-487줄, -72.6%)
- **FocusModeScreen.kt**: 635줄 → 147줄 (-488줄, -76.9%)
- **총 감소**: 1,984줄 → 588줄 (-1,396줄, -70.4%)
- **생성된 컴포넌트/ViewModel**: 9개

### 1. ReminderViewModel 리팩토링 (Facade Pattern)
**생성된 ViewModel**:
- **ReminderCrudViewModel.kt** (292줄): CRUD 핵심 기능
- **ReminderSearchViewModel.kt** (115줄): 검색/필터/정렬
- **ReminderAnalyticsViewModel.kt** (160줄): ML 카테고리 제안, 완료 패턴 분석

**기술적 특징**:
- Facade Pattern으로 하위 호환성 유지
- 기능별 책임 분리 (SRP 준수)
- 기존 테스트 100% 유지

### 2. EisenhowerMatrixScreen 리팩토링
**생성된 컴포넌트**:
- **QuadrantCard.kt** (318줄): 쿼드런트 카드, 리마인더 카드, 통계
- **TrendAnalysisDialog.kt** (195줄): 트렌드 다이얼로그, 차트, 시간대별 분포

### 3. FocusModeScreen 리팩토링
**생성된 컴포넌트**:
- **FocusTimerCard.kt** (277줄): 타이머 카드, 원형 타이머
- **FocusDndSettingsCard.kt** (128줄): DND 설정 카드
- **FocusStatsCard.kt** (77줄): 통계 카드
- **FocusSessionHistoryItem.kt** (66줄): 세션 히스토리

### 기술적 상세
- **패턴**: Facade Pattern, State hoisting, 컴포넌트 추출
- **테스트**: 326/326 통과 (100% ✅)
- **빌드**: 성공
- **커밋**: 3개 (각 화면별 분리 커밋)
- **목표 대비**: 모든 목표 초과 달성 (ReminderViewModel: 목표 300줄 → 실제 257줄, EisenhowerMatrixScreen: 목표 320줄 → 실제 184줄, FocusModeScreen: 목표 285줄 → 실제 147줄)

---

## 📈 코드 리뷰 결과 (2025-10-16 업데이트)

### 현재 대형 파일 분석 (상위 10개)
| 파일 | 줄 수 | 분류 | 리팩토링 우선순위 |
|------|------|------|------------------|
| SettingsScreen.kt | 699 | UI 화면 | 🟢 낮음 (이미 잘 구조화됨) |
| MainActivity.kt | 690 | 네비게이션 | ✅ 완료 (v1.68.2) |
| ReminderDatabase.kt | 674 | 마이그레이션 | 🟢 낮음 (불가피) |
| AddEditReminderScreen.kt | 648 | UI 화면 | ✅ 완료 (v1.68.2) |
| PomodoroScreen.kt | 463 | UI 화면 | 🟢 낮음 |
| HomeScreen.kt | 433 | UI 화면 | 🟢 낮음 |
| StatisticsScreen.kt | 420 | UI 화면 | 🟢 낮음 |
| ReminderViewModel.kt | 257 | ViewModel | ✅ 완료 (v1.68.3) |
| EisenhowerMatrixScreen.kt | 184 | UI 화면 | ✅ 완료 (v1.68.3) |
| FocusModeScreen.kt | 147 | UI 화면 | ✅ 완료 (v1.68.3) |

### v1.68.3 리팩토링 완료 ✅

#### ✅ ReminderViewModel.kt (678줄 → 257줄)
**완료 내용**:
- Facade Pattern으로 내부 위임 구조 구축
- ReminderCrudViewModel, ReminderSearchViewModel, ReminderAnalyticsViewModel 분리
- 하위 호환성 유지하며 책임 분리 달성

#### ✅ EisenhowerMatrixScreen.kt (671줄 → 184줄)
**완료 내용**:
- QuadrantCard, TrendAnalysisDialog 컴포넌트 추출
- 쿼드런트 카드 재사용성 극대화

#### ✅ FocusModeScreen.kt (635줄 → 147줄)
**완료 내용**:
- FocusTimerCard, FocusDndSettingsCard, FocusStatsCard, FocusSessionHistoryItem 추출
- 목표(285줄) 대비 48% 추가 감소 달성

---

## 🔥 다음 버전 계획

### 🎯 다음 우선순위 (순서대로)

#### Priority 1: Wear OS 앱 구현 🟠 (다음 우선순위)
- **목표**: 스마트워치 지원으로 사용성 대폭 향상
- **새 모듈**: `wear` 모듈 생성
- **주요 기능**:
  - Eisenhower Matrix 간소화 버전
  - 포커스 모드 워치 버전
  - 리마인더 빠른 완료 기능
- **예상 시간**: 6-7시간
- **TDD**: 필수

#### Priority 2: 시간 블로킹 (Time Blocking) 🟡
- **목표**: 캘린더와 통합하여 작업 시간 예약
- **주요 기능**:
  - Eisenhower Matrix의 SCHEDULE 쿼드런트 활용
  - 드래그 앤 드롭으로 시간대 배정
  - AI 기반 최적 시간 제안
- **예상 시간**: 5-6시간
- **TDD**: 필수

---

## 🚧 최근 완료 이력 (v1.68.0 ~ v1.68.3)

### v1.68.0: 카카오맵 SDK 통합 ✅ 🗺️
**완료됨** - 장소 검색 결과 지도 시각화, 위치 선택 UI
- 카카오맵 SDK 2.6.0 추가
- MapViewModel (TDD, 9개 테스트)
- MapScreen UI (전체 화면 지도, 위치 선택)
- Navigation 통합
- 위치 권한 처리
- 테스트: 326/326 통과 (100% ✅)

### v1.68.1: 지도 데이터 유지 버그 수정 ✅ 🐛
**완료됨** - rememberSaveable 적용
- AddEditReminderScreen 위치 필드에 rememberSaveable 적용
- 지도 화면 왕복 시 위치 데이터 유지
- 테스트: 326/326 통과 (100% ✅)

### v1.68.2: 코드 리팩토링 (컴포넌트 추출) ✅ 🧹
**완료됨** - MainActivity, AddEditReminderScreen 총 550줄 감소
- **LocationSearchSection.kt** (152줄 신규)
- **SubTaskSection.kt** (125줄 신규)
- **ImageAttachmentSection.kt** (135줄 신규)
- **ExposedDropdownField.kt** (53줄 신규)
- MainActivity: 987줄 → 690줄 (-297줄, -30.1%)
- AddEditReminderScreen: 963줄 → 648줄 (-315줄, -32.7%)
- 테스트: 326/326 통과 (100% ✅)

### v1.68.3: 대규모 리팩토링 (God Class 해체) ✅ 🧹✨
**완료됨** - ReminderViewModel, EisenhowerMatrixScreen, FocusModeScreen 총 1,396줄 감소
- **ReminderViewModel**: 678줄 → 257줄 (-421줄, -62.1%)
  - ReminderCrudViewModel.kt (292줄 신규)
  - ReminderSearchViewModel.kt (115줄 신규)
  - ReminderAnalyticsViewModel.kt (160줄 신규)
- **EisenhowerMatrixScreen**: 671줄 → 184줄 (-487줄, -72.6%)
  - QuadrantCard.kt (318줄 신규)
  - TrendAnalysisDialog.kt (195줄 신규)
- **FocusModeScreen**: 635줄 → 147줄 (-488줄, -76.9%)
  - FocusTimerCard.kt (277줄 신규)
  - FocusDndSettingsCard.kt (128줄 신규)
  - FocusStatsCard.kt (77줄 신규)
  - FocusSessionHistoryItem.kt (66줄 신규)
- 테스트: 326/326 통과 (100% ✅)

---

## 📅 다음 세션 제안

v1.68.3까지 완료! 다음 세션에서는:

### 1. **Wear OS 앱 구현** 🟠 (다음 우선순위)
- 스마트워치 지원으로 사용성 대폭 향상
- 새 wear 모듈 생성
- Eisenhower Matrix 간소화 버전
- 포커스 모드 워치 버전
- 리마인더 빠른 완료 기능

### 2. **시간 블로킹 (Time Blocking)** 🟡
- 캘린더와 통합하여 작업 시간 예약
- Eisenhower Matrix의 SCHEDULE 쿼드런트 활용
- 드래그 앤 드롭으로 시간대 배정
- AI 기반 최적 시간 제안

---

## 🚀 빠른 시작 가이드

### 다음 세션 시작 시 (자동 워크플로우):

**"다음 작업 진행해줘"라고 하면:**
1. ✅ **자동으로 테스트 먼저 실행** (`./gradlew test`)
2. ✅ 테스트 통과 확인
3. ✅ `NEXT_TASKS.md` 읽고 다음 작업 시작

**또는 특정 작업 지정:**
```
"Wear OS 앱 구현해줘 (TDD로)"
"시간 블로킹 구현해줘 (TDD로)"
```

**⚠️ 주의**: "다음 작업 진행해줘"는 자동으로 테스트를 먼저 실행합니다!

---

## 📝 개발 원칙

- **TDD 필수**: 모든 새 기능은 테스트 먼저 작성
- **커밋 메시지**: `type(scope): 한글 제목` 형식
- **버전 업데이트**: `app/build.gradle.kts`에서 versionCode/versionName 수정
- **문서화**: CHANGELOG.md, CLAUDE.md, README.md 동기화
- **API 키 관리**: local.properties에 저장 (절대 커밋 금지)

---

## ⚠️ 알려진 제약사항

### 완료된 기능의 제약사항
- **AI 스마트 추천**: 최소 학습 데이터 10개 필요
- **오프라인 모드**: 동기화 충돌 시나리오 테스트 필요
- **첨부파일**: Firebase Storage 무료 티어 5GB 제한
- **캘린더 통합**: Google Calendar API 10,000 requests/day 제한
- **Pomodoro Timer**: Bottom Navigation 통합 완료 ✅
- **Habit Tracker**: Bottom Navigation 통합 완료 ✅
- **Eisenhower Matrix**: Navigation 통합 완료 ✅
- **포커스 모드**: 기본 타이머 및 방해 금지 모드 완료 ✅
- **코드 리팩토링**: MainActivity, AddEditReminderScreen 완료 ✅

### Wear OS 구현 시 고려사항
- Wear OS 기기 테스트 필수
- 배터리 소모 최적화 (워치)
- 네트워크 비연결 시나리오 처리
- 폰 ↔ 워치 동기화 지연 시간

---

## 🔗 관련 파일

- **프로젝트 가이드**: `CLAUDE.md` ⭐ (먼저 읽기)
- **변경 이력**: `CHANGELOG.md`
- **빌드 설정**: `app/build.gradle.kts`
- **다음 작업**: `NEXT_TASKS.md` (현재 파일)
- **사용자 문서**: `README.md`

---

**Happy Coding! 🚀**

_v1.68.3까지 79개 버전, 27개 DB 마이그레이션을 완료했습니다. 코드 품질 및 재사용성 극대화!_

**주요 성과**:
- ✅ 79개 버전 릴리즈 (v1.0.0 ~ v1.68.3)
- ✅ 27번의 데이터베이스 마이그레이션
- ✅ TDD 기반 안정적인 코드베이스 (326개 테스트 100% 통과)
- ✅ Fake 구현 패턴으로 테스트 안정성 확보
- ✅ Firebase 실시간 동기화
- ✅ AI/ML 기반 스마트 추천
- ✅ 포모도로 & 습관 추적
- ✅ Bottom Navigation Bar (5개 메인 탭: Home, Statistics, Pomodoro, Focus, Habits)
- ✅ **Eisenhower Matrix (중요도×긴급도 매트릭스, Long Press 이동)** 🎯
- ✅ **포커스 모드 (집중 타이머, 세션 관리, 방해 금지 모드)** 🎯🔕
- ✅ **성능 최적화 (Room 인덱스, 앱 시작 15-20ms 개선)** ⚡
- ✅ **UI 일관성 (Material 3, 8dp 그리드 99% 준수)** 🎨
- ✅ **접근성 (WCAG AA 100% 준수, TalkBack 완벽 지원)** ♿
- ✅ **온보딩 개선 (6개 페이지, 한국어, 신규 기능 소개)** 🎓
- ✅ **코드 품질 개선 (v1.59.0~v1.62.0: Lint Error 0개 달성)** 🧹
- ✅ **Bottom Navigation 통합 (v1.63.0: TopAppBar 아키텍처 개선)** 🎨
- ✅ **테스트 함수명 표준화 (v1.63.1: 38개 파일, 370+ 함수 영어 변환)** ✅
- ✅ **RecurrencePattern 레거시 제거 (v1.64.0: 새 RecurrenceRule 시스템, 646줄 감소)** 🔄
- ✅ **카카오 로컬 API (v1.67.0: 장소 검색, 실시간 자동완성, TDD)** 📍
- ✅ **카카오맵 SDK (v1.68.0: 지도 시각화, 위치 선택 UI, TDD)** 🗺️
- ✅ **코드 리팩토링 (v1.68.2~v1.68.3: 총 1,946줄 감소, 컴포넌트/ViewModel 13개 생성)** 🧹✨
- ✅ 다국어 지원 (한/영/중)
- ✅ Material 3 디자인

**다음 우선순위**:
1. 🟠 **Wear OS 앱 구현** (스마트워치 지원, 생산성 극대화)
2. 🟡 **시간 블로킹** (캘린더 통합 작업 예약)

**⭐ v1.68.3 하이라이트**:
- **대규모 코드 리팩토링 완료**: 3개 대형 파일 총 1,396줄 감소 (-70.4%)
- **God Class 해체**: ReminderViewModel Facade Pattern 적용, 책임 분리
- **9개 컴포넌트/ViewModel 생성**: 재사용성 및 유지보수성 극대화
- **모든 목표 초과 달성**: ReminderViewModel (257줄 vs 목표 300줄), EisenhowerMatrixScreen (184줄 vs 목표 320줄), FocusModeScreen (147줄 vs 목표 285줄)
- **테스트 100% 유지**: 326/326 통과, 빌드 성공
- **개발자 경험 극대화**: 코드 가독성 향상, 단일 책임 원칙 준수, 하위 호환성 유지
