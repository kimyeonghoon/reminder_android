# 다음 작업 계획

> 마지막 업데이트: 2025-10-11 (v1.48.0 완료)
> **📌 다음 세션 시작 시 CLAUDE.md를 먼저 읽으세요!**

---

## 📊 현재 프로젝트 현황

- **최신 버전**: v1.48.0 (versionCode 54)
- **DB 버전**: v23
- **총 릴리즈**: 54개 버전
- **테스트 커버리지**: 240/240 통과 (100% ✅)

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

---

## 🔥 다음 버전 계획 (v1.48.0 이후)

### 🎯 향후 개발 방향

현재 v1.48.0까지 완료되어 핵심 기능, Eisenhower Matrix, 그리고 AI 긴급도 예측까지 모두 완료되었습니다. 향후 버전에서는 다음 영역에 집중할 수 있습니다:

#### 1. ~~Navigation 통합~~ ✅ 완료됨 (v1.47.0)
- ~~**Eisenhower Matrix 화면을 MainActivity Navigation에 추가**~~
  - ✅ MainActivity.kt에서 navController 라우팅 추가 완료
  - ✅ HomeScreen TopAppBar에 GridOn 아이콘 버튼 진입점 추가
  - ✅ 화면 전환 애니메이션 (슬라이드 + 페이드) 적용

#### 2. ~~코드 품질 및 안정성 개선~~ ✅ 완료됨 (v1.47.0)
- ✅ **모든 UI Deprecation warnings 수정 완료** (9개 파일):
  - ✅ `AddEditReminderScreen.kt`: menuAnchor() → MenuAnchorType.PrimaryNotEditable (2곳)
  - ✅ `EisenhowerMatrixScreen.kt`: Icons.Default.ArrowBack → AutoMirrored
  - ✅ 기타 5개 화면 ArrowBack 아이콘 업데이트
  - ✅ `SharedListsScreen.kt`: Icons.Default.List → AutoMirrored
  - ✅ `SortDropdown.kt`: menuAnchor() → MenuAnchorType.PrimaryNotEditable
  - ✅ `FilterChips.kt`: Icons.Default.VolumeUp → AutoMirrored
  - ✅ `FilterScreen.kt`: Icons.Default.VolumeUp → AutoMirrored
  - ✅ `HomeScreen.kt`: SearchBar API 마이그레이션 (inputField 파라미터)
- ✅ Material 3 최신 API 100% 적용
- ✅ RTL 언어 지원 개선 (AutoMirrored 아이콘)
- 전체 화면 일관성 최종 검토 및 Material 3 디자인 정교화
- 성능 최적화 및 메모리 누수 점검

#### 3. 고급 생산성 기능
- **포커스 모드** (방해 금지 모드, 특정 앱 차단)
  - Eisenhower Matrix의 DO_FIRST 쿼드런트와 연동
  - 집중해야 할 시간에 방해 요소 차단
- **목표 기반 리마인더** (장기 목표 → 하위 작업 자동 생성)
  - AI 기반 작업 분해 제안
  - Eisenhower Matrix로 우선순위 자동 배정
- **시간 블로킹** (Time Blocking)
  - 캘린더와 통합하여 작업 시간 예약
  - Eisenhower Matrix의 SCHEDULE 쿼드런트 활용

#### 4. 소셜 및 협업 강화
- 팀 작업 공간 (기존 협업 기능 확장)
- 댓글 및 멘션 시스템
- 활동 피드 (누가 무엇을 했는지)
- Eisenhower Matrix 공유 (팀원들과 우선순위 공유)

#### 5. 웨어러블 및 크로스 플랫폼
- Wear OS 앱 (스마트워치 지원)
- 태블릿 최적화 (2단 레이아웃)
- 웹 버전 (PWA)

#### 6. 인공지능 고도화
- 자연어 처리 (NLP) 기반 리마인더 생성
- 일정 최적화 제안 (스케줄링 AI)
- 음성 어시스턴트 통합 (Google Assistant, Bixby)
- ~~**Eisenhower Matrix 자동 분류 개선**~~ ✅ 부분 완료 (v1.48.0)
  - ✅ 제목/설명 분석으로 긴급도 자동 예측 (키워드 기반)
  - ⏳ 과거 데이터 학습으로 정확도 향상 (향후)

---

## 🚧 최근 완료 이력 (v1.46.0 ~ v1.48.0)

### v1.46.0: Bottom Navigation Bar ✅
**완료됨** - 5개 메인 탭(Home, Statistics, Pomodoro, Habits, Settings) 통합

### v1.46.1: HomeScreen UI 정리 ✅
**완료됨** - 중복 버튼 제거, Bottom Navigation 일관성 향상

### v1.46.2: Bottom Navigation 화면 UI 정리 ✅
**완료됨** - 뒤로가기 버튼 제거, 중복 메뉴 제거

### v1.46.3: Compose API 최신화 ✅
**완료됨** - Divider → HorizontalDivider, LinearProgressIndicator 람다 변환

### v1.46.4: 컴파일 경고 수정 ✅
**완료됨** - menuAnchor() deprecated 수정, 사용하지 않는 파라미터 제거

### v1.47.0: Eisenhower Matrix + Navigation 통합 + 코드 품질 개선 ✅ 🎯
**완료됨** - TDD 방식으로 생산성 매트릭스 구현 및 전체 통합
- **긴급도(Urgency) 필드 추가**: LOW, MEDIUM, HIGH
- **4개 쿼드런트 자동 분류**:
  - Q1 (DO_FIRST): 중요하고 긴급함 - 즉시 처리
  - Q2 (SCHEDULE): 중요하지만 긴급하지 않음 - 계획 수립
  - Q3 (DELEGATE): 긴급하지만 중요하지 않음 - 위임
  - Q4 (DELETE): 중요하지도 긴급하지도 않음 - 제거/최소화
- **EisenhowerMatrixScreen.kt**: 2×2 그리드 UI (280줄)
- **쿼드런트별 색상 구분**: 빨강/파랑/노랑/초록
- **TDD**: EisenhowerMatrixTest.kt (10개 테스트 추가)
- **DB 마이그레이션**: v22 → v23 (urgency 컬럼, 인덱스 추가)
- **테스트 통과**: 223/223 (기존 213 + 신규 10)
- **✅ Navigation 통합 완료**:
  - MainActivity.kt에 eisenhower_matrix 라우트 추가
  - HomeScreen TopAppBar에 GridOn 아이콘 버튼 진입점 추가
  - 화면 전환 애니메이션 (슬라이드 + 페이드 300ms)
- **✅ 코드 품질 개선 완료**:
  - 9개 파일 모든 UI Deprecation warnings 수정
  - Material 3 최신 API 100% 마이그레이션
    - AutoMirrored 아이콘 (ArrowBack, List, VolumeUp)
    - MenuAnchorType.PrimaryNotEditable
    - SearchBar inputField 파라미터
  - RTL 언어 지원 개선

### v1.48.0: AI 긴급도 자동 예측 ✅ 🤖
**완료됨** - Eisenhower Matrix 고도화 1단계: AI 기반 긴급도 예측 기능 구현
- **UrgencyPredictor 클래스 구현**:
  - 키워드 기반 NLP 분석 엔진
  - 한글/영어 지원 (긴급, urgent, asap, 지금, 바로, 오늘 등)
  - 3단계 긴급도 예측 (HIGH, MEDIUM, LOW)
  - 예측 근거 설명 제공 기능
- **AddEditReminderScreen 통합**:
  - 제목/설명 입력 시 자동으로 긴급도 예측
  - 🤖 AI 제안 버튼 표시 (예측값이 현재 값과 다를 때만)
  - 클릭 한 번으로 예측값 적용 가능
  - 간편 모드에서는 숨김 처리
- **TDD 방식 개발**:
  - 17개 테스트 먼저 작성 후 구현 (Red-Green-Refactor)
  - 총 240개 테스트 모두 통과 (223 + 17) ✅
- **키워드 분류**:
  - HIGH: 긴급, urgent, asap, 지금, 바로, 오늘, 당장, 즉시, 빨리, 급해, now, immediately, today, critical, emergency
  - MEDIUM: 이번 주, 이번주, 곧, 조만간, this week, soon, upcoming, shortly
  - LOW: 나중에, 언젠가, 여유, 천천히, later, someday, eventually, whenever, low priority
  - DEFAULT: MEDIUM (키워드 없을 때)

---

## 🚧 이전 계획 (v1.37.0 ~ v1.45.0) - 완료됨

### v1.37.0: AI 스마트 추천 🤖 ✅
**완료됨** - ML 기반 우선순위/카테고리 예측 시스템 구현

### v1.38.0 ~ v1.39.0: 오프라인 모드 & 첨부파일 고도화 ✅
**완료됨** - 작업 큐, 충돌 해결, PDF/DOC 첨부, OCR 기능 구현

### v1.40.0 ~ v1.40.1: 캘린더 통합 ✅
**완료됨** - CalendarContract API, 단방향/양방향 동기화, UI 구현

### v1.42.0: Quick Settings Tile ✅
**완료됨** - 알림창 빠른 추가, Material You 아이콘

### v1.43.0: Archive System ✅
**완료됨** - 완료 리마인더 자동 아카이브, 30일 자동 처리

### v1.44.0: Habit Tracker ✅
**완료됨** - 습관 추적, 연속 완료 Streak 관리

### v1.45.0: Pomodoro Timer ✅
**완료됨** - 25/5/15분 타이머, 통계, Streak 추적

### v1.45.1: 테스트 안정화 ✅
**완료됨** - Fake 구현 패턴 도입, 213/213 테스트 통과 (100%)

---

## 📅 다음 세션 제안

v1.48.0까지 AI 긴급도 자동 예측 완료! 다음 세션에서는:

### 1. ~~**Eisenhower Matrix Navigation 통합**~~ ✅ 완료됨
- ✅ MainActivity.kt에 EisenhowerMatrixScreen 라우팅 추가 완료
- ✅ HomeScreen TopAppBar에 GridOn 아이콘 진입점 추가 완료
- ✅ 화면 전환 애니메이션 적용 완료

### 2. ~~**코드 품질 개선**~~ ✅ 완료됨
- ✅ 모든 UI Deprecation warnings 수정 완료 (9개 파일)
- ✅ Material 3 최신 API 100% 적용
  - AutoMirrored 아이콘, MenuAnchorType, SearchBar inputField

### 3. ~~**AI 긴급도 자동 예측**~~ ✅ 완료됨 (v1.48.0)
- ✅ UrgencyPredictor 클래스 구현 (키워드 기반 NLP)
- ✅ AddEditReminderScreen에 AI 제안 버튼 통합
- ✅ 17개 테스트 작성 및 통과 (TDD 방식)

### 4. **Eisenhower Matrix 고도화 계속** (다음 우선순위) 🔴
- 쿼드런트별 통계 대시보드 (완료율, 평균 처리 시간, 트렌드)
- 쿼드런트 간 드래그 앤 드롭 이동
- 예상 시간: 2-3시간

### 4. **포커스 모드 구현** (고급 기능)
- Eisenhower Matrix DO_FIRST와 연동
- 방해 금지 모드, 앱 차단 기능
- 예상 시간: 4-5시간

### 5. **Wear OS 앱 구현** (장기 프로젝트)
- 스마트워치 지원으로 사용성 대폭 향상
- 새 wear 모듈 생성
- 예상 시간: 6-7시간

---

## 🚀 빠른 시작 가이드

### 다음 세션 시작 시 (자동 워크플로우):

**"다음 작업 진행해줘"라고 하면:**
1. ✅ **자동으로 테스트 먼저 실행** (`./gradlew test`)
2. ✅ 테스트 통과 확인
3. ✅ `NEXT_TASKS.md` 읽고 다음 작업 시작

**또는 특정 작업 지정:**
```
"Eisenhower Matrix를 MainActivity에 통합해줘"
"남은 deprecated warnings 수정해줘"
"포커스 모드 구현해줘 (TDD로)"
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
- **Eisenhower Matrix**: Navigation 통합 미완료 (다음 작업 권장) 🔴

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

_v1.47.0까지 53개 버전, 23개 DB 마이그레이션을 완료했습니다. Eisenhower Matrix로 생산성이 한 단계 더 업그레이드되었습니다!_

**주요 성과**:
- ✅ 53개 버전 릴리즈 (v1.0.0 ~ v1.47.0)
- ✅ 23번의 데이터베이스 마이그레이션
- ✅ TDD 기반 안정적인 코드베이스 (223개 테스트 100% 통과)
- ✅ Fake 구현 패턴으로 테스트 안정성 확보
- ✅ Firebase 실시간 동기화
- ✅ AI/ML 기반 스마트 추천
- ✅ 포모도로 & 습관 추적
- ✅ Bottom Navigation Bar (5개 메인 탭, UI 일관성 완벽 정리)
- ✅ **Eisenhower Matrix (중요도×긴급도 매트릭스)** 🎯
- ✅ 다국어 지원 (한/영/중)
- ✅ Material 3 디자인

**다음 우선순위**:
1. 🔴 **Eisenhower Matrix 고도화** (AI 예측, 드래그 앤 드롭, 통계)
2. 🟡 포커스 모드 구현 (DO_FIRST 연동, 방해 금지)
3. 🟢 Wear OS 앱 구현 (스마트워치 지원)

**⭐ v1.47.0 하이라이트**: Eisenhower Matrix로 생산성 혁명! 중요하고 긴급한 일을 한눈에 파악하고 효율적으로 관리하세요. Navigation 통합 완료로 즉시 사용 가능!
