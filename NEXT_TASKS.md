# 다음 작업 계획

> 마지막 업데이트: 2025-10-15 (v1.68.2 완료)
> **📌 다음 세션 시작 시 CLAUDE.md를 먼저 읽으세요!**

---

## 📊 현재 프로젝트 현황

- **최신 버전**: v1.68.2 (versionCode 75, DB v27)
- **DB 버전**: v27
- **총 릴리즈**: 78개 버전
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

---

## 🔥 최근 완료: v1.68.2 코드 리팩토링 ✅ 🧹

**완료일**: 2025-10-15
**목적**: 코드 크기 감소 및 재사용성 향상

### 성과 요약
- **MainActivity.kt**: 987줄 → 690줄 (-297줄, -30.1%)
- **AddEditReminderScreen.kt**: 963줄 → 648줄 (-315줄, -32.7%)
- **총 감소**: 550줄 (-28.2%)
- **생성된 재사용 컴포넌트**: 4개

### 생성된 컴포넌트
1. **LocationSearchSection.kt** (152줄)
   - 카카오 장소 검색 UI
   - 실시간 자동완성
   - 지오펜싱 상태 표시
   - 지도 네비게이션

2. **SubTaskSection.kt** (125줄)
   - 드래그 앤 드롭 재정렬
   - 서브태스크 완료/삭제
   - 새 서브태스크 추가

3. **ImageAttachmentSection.kt** (135줄)
   - 이미지 첨부 관리
   - Coil 이미지 로딩
   - 이미지 추가/삭제/보기

4. **ExposedDropdownField.kt** (53줄)
   - 제네릭 드롭다운 컴포넌트
   - Priority, Urgency, Advance Notification에 적용
   - Material 3 디자인

### 기술적 상세
- **패턴**: State hoisting, 컴포넌트 추출
- **테스트**: 326/326 통과 (100% ✅)
- **빌드**: 성공
- **커밋**: 3개 (각 컴포넌트별 분리 커밋)

---

## 📈 코드 리뷰 결과 (2025-10-15)

### 현재 대형 파일 분석 (상위 10개)
| 파일 | 줄 수 | 분류 | 리팩토링 우선순위 |
|------|------|------|------------------|
| SettingsScreen.kt | 699 | UI 화면 | 🟢 낮음 (이미 잘 구조화됨) |
| MainActivity.kt | 690 | 네비게이션 | ✅ 완료 (v1.68.2) |
| ReminderViewModel.kt | 678 | ViewModel | 🟡 중간 (분리 가능) |
| ReminderDatabase.kt | 674 | 마이그레이션 | 🟢 낮음 (불가피) |
| EisenhowerMatrixScreen.kt | 671 | UI 화면 | 🟡 중간 (컴포넌트 추출 가능) |
| AddEditReminderScreen.kt | 648 | UI 화면 | ✅ 완료 (v1.68.2) |
| FocusModeScreen.kt | 635 | UI 화면 | 🟡 중간 (컴포넌트 추출 가능) |
| PomodoroScreen.kt | 463 | UI 화면 | 🟢 낮음 |
| HomeScreen.kt | 433 | UI 화면 | 🟢 낮음 |
| StatisticsScreen.kt | 420 | UI 화면 | 🟢 낮음 |

### 리팩토링 권장사항

#### 1. ReminderViewModel.kt (678줄) 🟡
**현재 상태**:
- 단일 ViewModel에 너무 많은 책임 집중
- CRUD, 필터링, 정렬, 템플릿, 스누즈, TTS, ML 등 모두 포함

**권장 작업** (우선순위 중):
- 기능별 ViewModel 분리 고려:
  - `ReminderCrudViewModel`: CRUD 핵심 기능
  - `ReminderFilterViewModel`: 필터링/정렬/검색
  - `ReminderAnalyticsViewModel`: 완료 패턴 분석, ML 추천
- 이미 분리된 ViewModel 활용:
  - SubTaskViewModel ✅
  - AttachmentViewModel ✅
  - TemplateViewModel ✅
  - FilterViewModel ✅

**예상 효과**: 300~400줄 감소

#### 2. EisenhowerMatrixScreen.kt (671줄) 🟡
**현재 상태**:
- QuadrantCard, TrendAnalysisDialog, SimpleTrendChart 등 큰 컴포넌트 포함
- 중복 코드: 4개 쿼드런트 카드 생성

**권장 작업** (우선순위 중):
- 컴포넌트 파일 분리:
  - `QuadrantCard.kt` (~150줄)
  - `TrendAnalysisDialog.kt` (~120줄)
  - `QuadrantStatistics.kt` (~80줄)

**예상 효과**: 350줄 감소 → 320줄

#### 3. FocusModeScreen.kt (635줄) 🟡
**현재 상태**:
- TimerCard, CircularTimer, DndSettingsCard 등 큰 컴포넌트
- 잘 구조화되어 있지만 파일 크기가 큼

**권장 작업** (우선순위 낮음):
- 컴포넌트 파일 분리:
  - `FocusTimerCard.kt` (~200줄)
  - `DndSettingsCard.kt` (~100줄)
  - `CircularTimer.kt` (~50줄)

**예상 효과**: 350줄 감소 → 285줄

#### 4. SettingsScreen.kt (699줄) 🟢
**현재 상태**:
- 이미 잘 구조화됨 (섹션별 함수 분리)
- ThemeSection, FontSizeSection, NotificationSection 등

**권장 작업**: 없음 (현재 상태 유지)

---

## 🔥 다음 버전 계획

### 🎯 다음 우선순위 (순서대로)

#### Priority 1: ReminderViewModel 리팩토링 🟡
- **목표**: 678줄 → 300줄 (55% 감소)
- **방법**: 기능별 ViewModel 분리
- **예상 시간**: 3-4시간
- **TDD**: 기존 테스트 100% 유지
- **영향도**: 중간 (기존 ViewModel 참조 업데이트 필요)

#### Priority 2: EisenhowerMatrixScreen 리팩토링 🟡
- **목표**: 671줄 → 320줄 (52% 감소)
- **방법**: 대형 컴포넌트 파일 분리
- **예상 시간**: 2-3시간
- **TDD**: UI 테스트 유지
- **영향도**: 낮음 (화면 내부 리팩토링)

#### Priority 3: FocusModeScreen 리팩토링 🟡
- **목표**: 635줄 → 285줄 (55% 감소)
- **방법**: 타이머/DND 컴포넌트 분리
- **예상 시간**: 2시간
- **TDD**: UI 테스트 유지
- **영향도**: 낮음 (화면 내부 리팩토링)

#### Priority 4: Wear OS 앱 구현 🟠
- **목표**: 스마트워치 지원으로 사용성 대폭 향상
- **새 모듈**: `wear` 모듈 생성
- **주요 기능**:
  - Eisenhower Matrix 간소화 버전
  - 포커스 모드 워치 버전
  - 리마인더 빠른 완료 기능
- **예상 시간**: 6-7시간
- **TDD**: 필수

#### Priority 5: 시간 블로킹 (Time Blocking) 🟡
- **목표**: 캘린더와 통합하여 작업 시간 예약
- **주요 기능**:
  - Eisenhower Matrix의 SCHEDULE 쿼드런트 활용
  - 드래그 앤 드롭으로 시간대 배정
  - AI 기반 최적 시간 제안
- **예상 시간**: 5-6시간
- **TDD**: 필수

---

## 🚧 최근 완료 이력 (v1.68.0 ~ v1.68.2)

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

---

## 📅 다음 세션 제안

v1.68.2까지 완료! 다음 세션에서는:

### 1. **ReminderViewModel 리팩토링** 🟡 (다음 우선순위)
- 678줄 → 300줄 목표
- 기능별 ViewModel 분리
- 기존 테스트 100% 유지

### 2. **EisenhowerMatrixScreen 리팩토링** 🟡
- 671줄 → 320줄 목표
- 대형 컴포넌트 파일 분리

### 3. **FocusModeScreen 리팩토링** 🟡
- 635줄 → 285줄 목표
- 타이머/DND 컴포넌트 분리

### 4. **Wear OS 앱 구현** 🟠
- 스마트워치 지원
- 새 wear 모듈 생성
- Eisenhower Matrix 간소화 버전
- 포커스 모드 워치 버전

---

## 🚀 빠른 시작 가이드

### 다음 세션 시작 시 (자동 워크플로우):

**"다음 작업 진행해줘"라고 하면:**
1. ✅ **자동으로 테스트 먼저 실행** (`./gradlew test`)
2. ✅ 테스트 통과 확인
3. ✅ `NEXT_TASKS.md` 읽고 다음 작업 시작

**또는 특정 작업 지정:**
```
"ReminderViewModel 리팩토링해줘 (TDD로)"
"EisenhowerMatrixScreen 컴포넌트 분리해줘"
"FocusModeScreen 리팩토링해줘"
"Wear OS 앱 구현해줘 (TDD로)"
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

_v1.68.2까지 78개 버전, 27개 DB 마이그레이션을 완료했습니다. 코드 품질 및 재사용성 극대화!_

**주요 성과**:
- ✅ 78개 버전 릴리즈 (v1.0.0 ~ v1.68.2)
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
- ✅ **코드 리팩토링 (v1.68.2: 550줄 감소, 재사용 컴포넌트 4개 생성)** 🧹
- ✅ 다국어 지원 (한/영/중)
- ✅ Material 3 디자인

**다음 우선순위**:
1. 🟡 **ReminderViewModel 리팩토링** (678줄 → 300줄, 기능별 분리)
2. 🟡 **EisenhowerMatrixScreen 리팩토링** (671줄 → 320줄, 컴포넌트 분리)
3. 🟡 **FocusModeScreen 리팩토링** (635줄 → 285줄, 컴포넌트 분리)
4. 🟠 **Wear OS 앱 구현** (스마트워치 지원, 생산성 극대화)
5. 🟡 **시간 블로킹** (캘린더 통합 작업 예약)

**⭐ v1.68.2 하이라이트**:
- **코드 크기 대폭 감소**: MainActivity (-297줄), AddEditReminderScreen (-315줄), 총 550줄 감소
- **재사용 컴포넌트 생성**: LocationSearchSection, SubTaskSection, ImageAttachmentSection, ExposedDropdownField
- **코드 재사용성 향상**: 공통 패턴 추출, State hoisting 적용
- **테스트 100% 유지**: 326/326 통과, 빌드 성공
- **개발자 경험 개선**: 코드 가독성 향상, 유지보수 용이성 증대
