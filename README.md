# 📝 Reminder

> 스마트하고 강력한 Android 리마인더 앱

Reminder는 당신의 일상을 체계적으로 관리할 수 있는 네이티브 Android 애플리케이션입니다.
TDD(Test-Driven Development)로 개발되어 안정성과 품질이 검증되었으며, 최신 Android 개발 기술을 사용하여 빠르고 부드러운 경험을 제공합니다.

## ✨ 주요 기능

### 📋 기본 기능
- ✅ **할 일 관리** - 할 일을 추가, 수정, 완료 표시
- 🎯 **우선순위 설정** - 높음/중간/낮음으로 중요도 구분
- 🏷️ **카테고리** - 할 일을 카테고리별로 정리
- 🔍 **스마트 검색** - 제목, 설명, 카테고리로 빠른 검색

### ⏰ 알림 & 반복
- 📅 **날짜/시간 선택** - 직관적인 날짜/시간 피커
- 🔔 **알림 기능** - 설정한 시간에 알림 수신
- 🔄 **반복 리마인더** - 매일, 매주, 매월, 매년 반복 설정
  - 요일별 선택 (주간 반복)
  - 반복 간격 설정
  - 종료 날짜 지정
- 🔁 **자동 반복 스케줄링** - 알림 발생 시 다음 일정 자동 설정

### 📊 필터링 & 정렬
- 🎨 **우선순위 필터** - 높음/중간/낮음으로 필터링
- 📆 **날짜 필터** - 오늘, 이번 주, 이번 달, 기한 초과
- 🔢 **다양한 정렬** - 날짜, 우선순위, 제목, 생성일 기준 정렬

### 🎨 UI/UX
- 📱 **Material Design 3** - 아름답고 직관적인 UI
- 🌓 **다크 모드** - Android 12+ 동적 컬러 지원
- ✨ **부드러운 애니메이션** - 리스트 아이템 추가/삭제/재배치 애니메이션
- 🎯 **최적화된 성능** - 재구성 최소화 및 DB 인덱스 최적화

### ☁️ 동기화 & 데이터
- 💾 **로컬 저장** - Room Database로 안전한 오프라인 저장
- ☁️ **Firebase 동기화** - 실시간 클라우드 동기화 (선택 사항)
- 📈 **통계 대시보드** - 완료율 및 진행 상황 확인
- ⚙️ **설정** - 앱 테마 및 동작 커스터마이징

## 📱 스크린샷

<!-- 추후 추가 예정 -->
_스크린샷은 곧 추가됩니다_

## 🚀 시작하기

### 요구사항

- Android 8.0 (API 26) 이상
- Android Studio Arctic Fox 이상 (개발자용)

### 설치 방법

1. **APK 다운로드** (릴리즈 페이지에서)
   ```
   곧 제공됩니다
   ```

2. **직접 빌드하기**
   ```bash
   # 저장소 클론
   git clone https://github.com/yourusername/reminder.git
   cd reminder

   # Android Studio에서 열거나 명령줄로 빌드
   ./gradlew assembleDebug

   # APK 위치: app/build/outputs/apk/debug/app-debug.apk
   ```

## 🎯 사용 방법

### 기본 사용법

1. **리마인더 추가하기**
   - 오른쪽 하단의 `+` (FAB) 버튼을 눌러요
   - 제목 입력 (필수)
   - 설명, 카테고리 입력 (선택)
   - 우선순위 선택 (높음/중간/낮음)
   - 날짜와 시간 설정 (알림을 받으려면)
   - "Save" 버튼을 누르면 완료!

2. **반복 리마인더 설정**
   - 리마인더 추가/수정 화면에서 "Recurrence" 섹션 찾기
   - 반복 패턴 선택: None, Daily, Weekly, Monthly, Yearly
   - 반복 간격 설정 (예: 2일마다, 3주마다)
   - 주간 반복 시 요일 선택 가능 (월/화/수/목/금/토/일)
   - 종료 날짜 지정 (선택 사항)

3. **리마인더 완료/관리**
   - 체크박스를 눌러 완료 표시 (취소선 표시됨)
   - 리마인더를 터치하면 수정 가능
   - 휴지통 아이콘으로 삭제

4. **검색 & 필터링**
   - 상단 검색 아이콘으로 검색
   - 우선순위/날짜 필터 칩으로 필터링
   - 정렬 옵션으로 리스트 정렬

5. **통계 & 설정**
   - 통계 아이콘으로 진행 상황 확인
   - 설정 아이콘으로 앱 커스터마이징

## 🛠️ 기술 스택

이 앱은 최신 Android 개발 베스트 프랙티스를 따릅니다:

### 핵심 기술
- **언어**: Kotlin 1.9.20
- **UI**: Jetpack Compose (Material Design 3)
- **아키텍처**: MVVM (Model-View-ViewModel)
- **데이터베이스**: Room (with TypeConverters)
- **비동기 처리**: Coroutines & Flow
- **의존성 주입**: Manual DI (Factory Pattern)

### 주요 라이브러리
- **Navigation**: Navigation Compose
- **상태 관리**: StateFlow, derivedStateOf
- **알림**: AlarmManager (정확한 알람)
- **동기화**: Firebase Firestore
- **인증**: Firebase Auth (Google Sign-In)
- **테스트**: JUnit4, Mockito, Compose Testing

### 개발 원칙
- **TDD**: 테스트 주도 개발 (유닛 테스트 먼저 작성)
- **Clean Architecture**: 레이어 분리 (UI → ViewModel → Repository → DAO)
- **성능 최적화**: DB 인덱스, Compose 재구성 최소화
- **접근성**: ContentDescription, 시맨틱 트리

## 🧪 테스트

이 프로젝트는 TDD로 개발되었으며 포괄적인 테스트 커버리지를 제공합니다:

### 테스트 실행
```bash
# 유닛 테스트 (JVM)
./gradlew testDebugUnitTest

# 통합 테스트 (디바이스/에뮬레이터 필요)
./gradlew connectedAndroidTest

# 빌드 & 설치
./gradlew installDebug
```

### 테스트 구성
- **유닛 테스트**: ViewModel 로직, 비즈니스 로직, 유틸리티
  - `AlarmSchedulerCalculationTest`: 반복 알람 계산 로직 (12개)
  - `ReminderViewModelTest`: ViewModel 로직
  - `ReminderRepositoryTest`: Repository 로직

- **통합 테스트**: Database, DAO 쿼리
  - `ReminderDaoTest`: Room DAO 쿼리 검증
  - `FirebaseSyncTest`: Firebase 동기화 검증

- **UI 테스트**: Compose 화면 상호작용
  - `HomeScreenTest`: 메인 화면 테스트 (12개)
  - `AddEditReminderScreenTest`: 추가/수정 화면 테스트 (18개)

## 📦 릴리즈 & 버전

### v1.7.0 (최신) - 2025-10-08
- ✨ 반복 리마인더 기능 추가 (Daily/Weekly/Monthly/Yearly)
- 🔁 자동 반복 스케줄링 구현
- ⚡ 성능 최적화 (DB 인덱스, Compose 재구성 최소화)
- 🎨 리스트 아이템 애니메이션 추가
- ✅ UI 테스트 30개 추가

### v1.6.0 - 2025-10-07
- 🎨 테마 커스터마이징
- ⚙️ 설정 화면 추가

### v1.5.0 - 2025-10-06
- 📊 통계 대시보드 구현
- 📈 완료율 시각화

### v1.4.0 - 2025-10-05
- 🎯 필터링 기능 (우선순위, 날짜)
- 🔢 정렬 옵션 추가

### v1.3.0 - 2025-10-04
- ☁️ Firebase 실시간 동기화
- 🔐 Google 로그인 통합

### v1.2.0 - 2025-10-03
- 🔔 알림 기능 구현
- ⏰ AlarmManager 통합

### v1.1.0 - 2025-10-02
- 📅 날짜/시간 선택 UI

### v1.0.0 - 2025-10-01
- 🎉 초기 릴리즈
- ✅ 기본 CRUD 기능

## 🤝 기여하기

기여를 환영합니다! 이슈나 풀 리퀘스트를 자유롭게 남겨주세요.

### 개발 가이드라인
이 프로젝트는 엄격한 코딩 규약을 따릅니다. 기여 전에 [`CLAUDE.md`](./CLAUDE.md)를 참고해주세요:

- **TDD 필수**: 테스트 먼저, 구현 나중
- **커밋 메시지**: `type(scope): 한글 제목` 형식
- **민감 정보**: API 키 등 커밋 금지
- **MVVM 준수**: 레이어 분리 엄수

### 기여 프로세스
1. Fork 하기
2. Feature 브랜치 생성 (`git checkout -b feature/amazing-feature`)
3. 테스트 작성 (TDD)
4. 변경사항 커밋 (`git commit -m 'feat(ui): 놀라운 기능 추가'`)
5. 브랜치에 Push (`git push origin feature/amazing-feature`)
6. Pull Request 열기

## 📄 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 참고하세요.

## 📮 문의

문제가 있거나 제안사항이 있으시면 [이슈](../../issues)를 열어주세요!

---

Made with ❤️ using Kotlin & Jetpack Compose
