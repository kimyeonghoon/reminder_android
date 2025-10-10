# NEXT_WORK.md

⚠️ **다음 세션 시작 시**: 이 문서를 읽고 작업 완료 후 삭제할 것!

---

## 📋 향후 5개 버전 구현 계획 (v1.32.0 ~ v1.36.0)

현재 완료: **v1.31.0** (테마 프리셋 확장)

---

## v1.32.0 - 고급 검색 및 필터링 시스템 🔍

### 목표
할 일이 많아질수록 원하는 항목을 빠르게 찾을 수 있도록 강력한 필터링 시스템 구축

### 구현 항목

#### 1. 복합 필터 엔진
**파일**: `app/src/main/java/com/reminder/filter/FilterEngine.kt` (신규)
- `data class ReminderFilter` 정의
  - `priorities: List<Priority>?`
  - `categories: List<String>?`
  - `tags: List<String>?`
  - `dateRange: DateRange?` (시작일, 종료일)
  - `isCompleted: Boolean?`
  - `hasLocation: Boolean?`
  - `hasWebLink: Boolean?`
  - `hasTts: Boolean?`
- `fun applyFilter(reminders: List<ReminderEntity>, filter: ReminderFilter): List<ReminderEntity>`
- TDD 방식: `FilterEngineTest.kt` 먼저 작성 (10개 테스트)

#### 2. 저장된 필터 (스마트 컬렉션)
**파일**: `app/src/main/java/com/reminder/data/entity/SavedFilterEntity.kt` (신규)
- Room 엔티티 추가
  - `id`, `name`, `icon`, `filterJson` (JSON으로 직렬화)
  - `createdAt`, `order`
- `SavedFilterDao.kt` 작성
- DB 버전: v12 → v13 (마이그레이션 필요)

#### 3. 필터 프리셋
**파일**: `app/src/main/java/com/reminder/filter/FilterPresets.kt` (신규)
- 기본 제공 프리셋 정의
  - "오늘": 오늘 마감
  - "이번 주": 7일 이내 마감
  - "중요": 높은 우선순위
  - "긴급": 오늘 마감 + 높은 우선순위
  - "미완료": isCompleted = false
  - "위치 설정됨": hasLocation = true
  - "반복 작업": hasRecurrence = true

#### 4. UI 구현
**파일**: `app/src/main/java/com/reminder/ui/screen/FilterScreen.kt` (신규)
- BottomSheet 기반 필터 UI
- 복합 필터 선택 (Chip 기반)
- 저장된 필터 목록 표시
- "필터 저장" 버튼

**파일**: `app/src/main/java/com/reminder/ui/components/FilterChips.kt` (신규)
- 빠른 필터 칩 (LazyRow)
- 프리셋 필터 칩
- 활성 필터 표시 및 제거

#### 5. HomeScreen 통합
**파일**: `app/src/main/java/com/reminder/ui/screen/HomeScreen.kt` (수정)
- 상단에 필터 칩 LazyRow 추가
- 필터 버튼 (아이콘: FilterList)
- FilterScreen 연결

#### 6. ViewModel 확장
**파일**: `app/src/main/java/com/reminder/viewmodel/ReminderViewModel.kt` (수정)
- `currentFilter: StateFlow<ReminderFilter?>` 추가
- `fun applyFilter(filter: ReminderFilter)`
- `fun clearFilter()`
- 필터 적용 시 자동으로 목록 업데이트

### Technical Details
- **Files Created**: 6개 (FilterEngine, SavedFilterEntity, SavedFilterDao, FilterPresets, FilterScreen, FilterChips)
- **Files Modified**: 3개 (ReminderViewModel, HomeScreen, ReminderDatabase)
- **Database**: v12 → v13 (SavedFilterEntity 테이블 추가)
- **Tests**: FilterEngineTest (10개), SavedFilterDaoTest (5개)
- **Lines**: +600 (approx.)

---

## v1.33.0 - 통계 대시보드 고도화 📊

### 목표
장기 트렌드 분석 및 목표 달성률 추적으로 생산성 인사이트 제공

### 구현 항목

#### 1. 장기 통계 계산
**파일**: `app/src/main/java/com/reminder/viewmodel/StatisticsViewModel.kt` (확장)
- `fun calculateMonthlyStats(year: Int): List<MonthlyStats>`
  - 각 월별 완료/미완료/완료율
- `fun calculateYearlyStats(): List<YearlyStats>`
  - 연도별 비교
- `fun getCategoryBreakdown(): Map<String, CategoryStats>`
  - 카테고리별 완료율, 평균 소요 시간

#### 2. 목표 설정 시스템
**파일**: `app/src/main/java/com/reminder/data/entity/GoalEntity.kt` (신규)
- Room 엔티티
  - `id`, `type` (DAILY, WEEKLY, MONTHLY)
  - `targetCount: Int` (목표 완료 개수)
  - `category: String?` (특정 카테고리 목표)
  - `startDate`, `endDate`
  - `isActive: Boolean`
- `GoalDao.kt` 작성
- DB 버전: v13 → v14

#### 3. 목표 달성률 계산
**파일**: `app/src/main/java/com/reminder/goal/GoalTracker.kt` (신규)
- `fun calculateProgress(goal: Goal): GoalProgress`
  - `currentCount`, `targetCount`, `percentage`
  - `isAchieved`, `remainingDays`
- TDD: `GoalTrackerTest.kt` (8개 테스트)

#### 4. 생산성 인사이트
**파일**: `app/src/main/java/com/reminder/analytics/ProductivityInsights.kt` (신규)
- `fun generateInsights(stats: Statistics): List<Insight>`
  - "이번 주 완료율이 지난주보다 15% 상승했어요!"
  - "가장 생산적인 시간대는 오전 10시입니다"
  - "업무 카테고리 완료율이 낮습니다 (45%)"
  - "3일 연속 목표 달성! 🎉"

#### 5. UI 구현
**파일**: `app/src/main/java/com/reminder/ui/screen/StatisticsScreen.kt` (대폭 확장)
- 탭 추가 (주간, 월간, 연간)
- 월별/연도별 차트 (MPAndroidChart 활용)
- 카테고리별 원형 차트 (PieChart)
- 목표 진행률 카드 (LinearProgressIndicator)
- 인사이트 카드 섹션

**파일**: `app/src/main/java/com/reminder/ui/components/GoalProgressCard.kt` (신규)
- 목표 진행률 시각화
- 남은 일수 표시
- 달성 시 축하 애니메이션

#### 6. 목표 설정 화면
**파일**: `app/src/main/java/com/reminder/ui/screen/GoalSettingScreen.kt` (신규)
- 목표 타입 선택 (일간/주간/월간)
- 목표 개수 입력
- 카테고리 선택 (선택사항)
- 기간 설정

### Technical Details
- **Files Created**: 6개 (GoalEntity, GoalDao, GoalTracker, ProductivityInsights, GoalProgressCard, GoalSettingScreen)
- **Files Modified**: 2개 (StatisticsViewModel, StatisticsScreen)
- **Database**: v13 → v14 (GoalEntity 테이블 추가)
- **Charts**: PieChart, BarChart 추가
- **Tests**: GoalTrackerTest (8개), ProductivityInsightsTest (5개)
- **Lines**: +700 (approx.)

---

## v1.34.0 - 위젯 2.0 📱

### 목표
다양한 크기와 인터랙티브 기능을 가진 차세대 홈 화면 위젯

### 구현 항목

#### 1. 위젯 크기 확장
**파일**: `app/src/main/java/com/reminder/widget/ReminderWidgetProvider.kt` (확장)
- Small (2x2): 오늘 할 일 개수만 표시
- Medium (4x2): 최대 5개 항목 표시 (기존 유지)
- Large (4x4): 최대 10개 항목 + 필터 버튼

#### 2. 위젯 설정 Activity
**파일**: `app/src/main/java/com/reminder/widget/WidgetConfigActivity.kt` (신규)
- 위젯 추가 시 설정 화면 표시
  - 테마 프리셋 선택 (10가지)
  - 필터 선택 (오늘, 이번 주, 중요 등)
  - 정렬 방식 선택
  - 표시할 최대 항목 수
- 설정을 SharedPreferences에 저장 (위젯 ID별로)

#### 3. 테마 프리셋 적용
**파일**: `app/src/main/res/layout/widget_*.xml` (확장)
- 10가지 테마별 RemoteViews 생성
- 배경 색상, 텍스트 색상 동적 변경
- 우선순위 인디케이터 색상 적용

#### 4. 인터랙티브 위젯
**파일**: `app/src/main/java/com/reminder/widget/WidgetActions.kt` (신규)
- "빠른 추가" 버튼 (Large 위젯만)
  - 클릭 시 간단한 입력 화면 표시
  - RemoteViews에 PendingIntent 추가
- 필터 전환 버튼 (Large 위젯만)
  - 오늘 ↔ 이번 주 ↔ 중요 순환

#### 5. 위젯별 필터 설정
**파일**: `app/src/main/java/com/reminder/widget/WidgetPreferences.kt` (신규)
- 위젯 ID별 설정 저장
  - `widgetId`, `themePreset`, `filter`, `sortBy`, `maxItems`
- `fun getWidgetConfig(widgetId: Int): WidgetConfig`
- `fun saveWidgetConfig(widgetId: Int, config: WidgetConfig)`

#### 6. 실시간 동기화 개선
**파일**: `app/src/main/java/com/reminder/widget/WidgetUpdateWorker.kt` (신규)
- WorkManager 기반 주기적 업데이트 (15분마다)
- 데이터 변경 시 즉시 업데이트 (기존 유지)

### Technical Details
- **Files Created**: 4개 (WidgetConfigActivity, WidgetActions, WidgetPreferences, WidgetUpdateWorker)
- **Files Modified**: 2개 (ReminderWidgetProvider, widget layouts)
- **Widget Sizes**: 3가지 (Small, Medium, Large)
- **Layouts**: widget_small.xml, widget_medium.xml, widget_large.xml
- **Tests**: WidgetPreferencesTest (6개)
- **Lines**: +500 (approx.)

---

## v1.35.0 - 반복 작업 고급 옵션 🔄

### 목표
유연하고 강력한 반복 패턴 설정으로 다양한 사용 사례 지원

### 구현 항목

#### 1. 고급 반복 패턴
**파일**: `app/src/main/java/com/reminder/data/entity/RecurrenceRule.kt` (신규)
- `data class RecurrenceRule` 정의
  - `type: RecurrenceType` (DAILY, WEEKLY, MONTHLY, CUSTOM)
  - `interval: Int` (2일마다, 3주마다 등)
  - `daysOfWeek: Set<DayOfWeek>?` (월, 수, 금)
  - `dayOfMonth: Int?` (매월 15일)
  - `weekOfMonth: Int?` (매월 첫째 주)
  - `monthsOfYear: Set<Month>?` (1월, 7월만)
- Room에서 JSON 변환 (TypeConverter)

#### 2. 예외 날짜 관리
**파일**: `app/src/main/java/com/reminder/data/entity/RecurrenceException.kt` (신규)
- Room 엔티티
  - `id`, `reminderId`, `exceptionDate`, `reason`
- `RecurrenceExceptionDao.kt` 작성
- DB 버전: v14 → v15

#### 3. 반복 종료 조건
**파일**: `app/src/main/java/com/reminder/recurrence/RecurrenceEnd.kt` (신규)
- `sealed class RecurrenceEnd`
  - `Never`: 종료 없음
  - `AfterOccurrences(count: Int)`: N회 후 종료
  - `OnDate(date: LocalDate)`: 특정 날짜에 종료

#### 4. 반복 스케줄 계산기
**파일**: `app/src/main/java/com/reminder/recurrence/RecurrenceScheduler.kt` (신규)
- `fun calculateNextOccurrences(rule: RecurrenceRule, start: LocalDate, limit: Int): List<LocalDate>`
- `fun isOccurrenceDate(rule: RecurrenceRule, date: LocalDate): Boolean`
- 예외 날짜 자동 스킵
- TDD: `RecurrenceSchedulerTest.kt` (15개 테스트)

#### 5. UI 구현
**파일**: `app/src/main/java/com/reminder/ui/screen/RecurrenceSettingScreen.kt` (신규)
- 반복 타입 선택 (라디오 버튼)
- 간격 설정 (NumberPicker)
- 요일 선택 (다중 선택 칩)
- 월/주 선택기
- 예외 날짜 추가 (달력)
- 종료 조건 설정
- 미리보기: 다음 5회 발생 날짜 표시

**파일**: `app/src/main/java/com/reminder/ui/components/RecurrencePreview.kt` (신규)
- 다음 발생 날짜 미리보기 카드
- "매주 월, 수, 금" 같은 자연어 요약

#### 6. AddEditReminderScreen 통합
**파일**: `app/src/main/java/com/reminder/ui/screen/AddEditReminderScreen.kt` (수정)
- 기존 단순 반복 → 고급 반복 설정 버튼
- RecurrenceSettingScreen 네비게이션

### Technical Details
- **Files Created**: 7개 (RecurrenceRule, RecurrenceException, RecurrenceExceptionDao, RecurrenceEnd, RecurrenceScheduler, RecurrenceSettingScreen, RecurrencePreview)
- **Files Modified**: 2개 (ReminderEntity, AddEditReminderScreen)
- **Database**: v14 → v15 (RecurrenceException 테이블, ReminderEntity에 recurrenceRule/recurrenceEnd 필드 추가)
- **Tests**: RecurrenceSchedulerTest (15개), RecurrenceRuleTest (8개)
- **Lines**: +800 (approx.)

---

## v1.36.0 - 협업 기능 👥

### 목표
리마인더를 다른 사용자와 공유하고 협업할 수 있는 기능

### 구현 항목

#### 1. 사용자 인증 강화
**파일**: `app/src/main/java/com/reminder/auth/AuthManager.kt` (확장)
- Firebase Authentication 통합 강화
- 이메일/비밀번호 로그인
- Google 소셜 로그인
- 사용자 프로필 관리

#### 2. 공유 리마인더 엔티티
**파일**: `app/src/main/java/com/reminder/data/entity/SharedReminderEntity.kt` (신규)
- Firestore 컬렉션 구조
  - `shared_reminders/{reminderId}/collaborators/{userId}`
  - `permission: Permission` (OWNER, EDITOR, VIEWER)
  - `sharedAt`, `sharedBy`

#### 3. 공유 리스트/프로젝트
**파일**: `app/src/main/java/com/reminder/data/entity/SharedListEntity.kt` (신규)
- Firestore 컬렉션
  - `shared_lists/{listId}`
  - `name`, `description`, `color`
  - `ownerId`, `members: List<Member>`
  - `reminders: List<reminderId>`

#### 4. 권한 관리
**파일**: `app/src/main/java/com/reminder/sharing/PermissionManager.kt` (신규)
- `enum class Permission { OWNER, EDITOR, VIEWER }`
- `fun hasPermission(userId: String, reminderId: String, required: Permission): Boolean`
- `fun grantPermission(userId: String, reminderId: String, permission: Permission)`
- `fun revokePermission(userId: String, reminderId: String)`

#### 5. 실시간 동기화
**파일**: `app/src/main/java/com/reminder/sync/SharedReminderSync.kt` (신규)
- Firestore Snapshot Listener
- 공유 리마인더 변경 실시간 수신
- 로컬 Room DB 자동 업데이트
- 충돌 해결 전략 (Last Write Wins)

#### 6. UI 구현
**파일**: `app/src/main/java/com/reminder/ui/screen/ShareScreen.kt` (신규)
- 이메일로 사용자 초대
- 권한 선택 (보기/편집)
- 현재 협업자 목록
- 권한 변경/제거

**파일**: `app/src/main/java/com/reminder/ui/screen/SharedListsScreen.kt` (신규)
- 공유 리스트 목록
- 새 공유 리스트 생성
- 리스트별 리마인더 표시

#### 7. 알림 시스템
**파일**: `app/src/main/java/com/reminder/notification/SharingNotifications.kt` (신규)
- 초대 알림
- 공유 리마인더 수정 알림
- 댓글 알림 (미래 확장)

#### 8. AddEditReminderScreen 통합
**파일**: `app/src/main/java/com/reminder/ui/screen/AddEditReminderScreen.kt` (수정)
- "공유" 버튼 추가
- 현재 협업자 표시 (아바타 칩)

### Technical Details
- **Files Created**: 8개 (AuthManager, SharedReminderEntity, SharedListEntity, PermissionManager, SharedReminderSync, ShareScreen, SharedListsScreen, SharingNotifications)
- **Files Modified**: 3개 (AddEditReminderScreen, HomeScreen, ReminderEntity)
- **Firebase**: Firestore 컬렉션 2개 (shared_reminders, shared_lists)
- **Authentication**: Firebase Auth 통합
- **Tests**: PermissionManagerTest (10개), SharedReminderSyncTest (8개)
- **Lines**: +1000 (approx.)

---

## 📅 예상 일정

- **v1.32.0** (고급 검색/필터): 다음 세션
- **v1.33.0** (통계 고도화): 다음 세션 + 1
- **v1.34.0** (위젯 2.0): 다음 세션 + 2
- **v1.35.0** (반복 작업): 다음 세션 + 3
- **v1.36.0** (협업 기능): 다음 세션 + 4

---

## 🎯 우선순위

1. **v1.32.0** - 고급 검색/필터 (필수, 사용성 대폭 향상)
2. **v1.33.0** - 통계 고도화 (높음, 생산성 인사이트)
3. **v1.34.0** - 위젯 2.0 (중간, UX 개선)
4. **v1.35.0** - 반복 작업 (중간, 고급 사용자용)
5. **v1.36.0** - 협업 기능 (낮음, 복잡도 높음, 서버 인프라 필요)

---

## ⚠️ 주의사항

- **TDD 원칙 준수**: 모든 비즈니스 로직은 테스트 먼저 작성
- **DB 마이그레이션**: 버전별로 순차적으로 진행 (v12→v13→v14→v15)
- **Firebase 비용**: v1.36.0 협업 기능은 Firestore 읽기/쓰기 비용 발생 고려
- **성능**: 필터링/통계 계산은 백그라운드 스레드에서 수행

---

## 📝 다음 세션 시작 방법

1. 이 문서(`NEXT_WORK.md`)를 읽고 확인
2. "v1.32.0 작업을 시작해줘"라고 요청
3. 작업 완료 후 이 문서 삭제: `git rm NEXT_WORK.md`

**Happy Coding! 🚀**
