# CLAUDE.md

이 파일은 Claude Code(claude.ai/code)가 이 저장소의 코드 작업 시 참고하는 가이드입니다.

## 프로젝트 개요

Reminder는 Kotlin과 Jetpack Compose로 구축된 네이티브 Android TODO 애플리케이션입니다. MVVM 아키텍처를 사용하며 로컬 데이터 저장을 위해 Room Database를 활용합니다.

## 빌드 및 실행 명령어

### 프로젝트 빌드
```bash
./gradlew build
```

### 테스트 실행
```bash
# 모든 유닛 테스트 실행
./gradlew test

# 모든 계측 테스트 실행 (에뮬레이터 또는 실제 기기 필요)
./gradlew connectedAndroidTest
```

### 디바이스/에뮬레이터에 설치
```bash
./gradlew installDebug
```

### 클린 빌드
```bash
./gradlew clean
```

## 아키텍처

### MVVM 패턴
- **Model**: `data/entity/ReminderEntity.kt` - Priority enum을 포함한 Room 엔티티
- **View**: `ui/screen/*` - Compose 화면들 (HomeScreen, AddEditReminderScreen)
- **ViewModel**: `viewmodel/ReminderViewModel.kt` - Kotlin Flow를 사용한 상태 관리

### 데이터 레이어
- **Room Database**: `ReminderDatabase.kt`의 싱글톤 패턴
- **DAO**: `ReminderDao.kt`는 반응형 업데이트를 위한 Flow 기반 쿼리 제공
- **Repository**: `ReminderRepository.kt`는 데이터 접근을 추상화
- **Type Converters**: `Converters.kt`는 LocalDateTime과 Priority enum 변환 처리

### 의존성 주입
`ReminderApplication.kt`를 통한 수동 DI:
- Database와 Repository는 애플리케이션 레벨에서 lazy 초기화됨
- ViewModel은 `ReminderViewModelFactory.kt`를 통해 Repository를 받음

### 네비게이션
두 개의 라우트를 가진 Navigation Compose:
- `"home"` - 메인 리스트 화면
- `"add_edit"` - 리마인더 생성/수정 화면

편집을 위한 상태는 MainActivity의 ReminderApp composable에서 `selectedReminder`로 관리됩니다.

### UI 컴포넌트
- **ReminderCard**: 체크박스, 우선순위 표시, 삭제 버튼이 있는 재사용 가능한 카드
- **Theme**: Material 3, Android 12+ 동적 컬러 지원
- 우선순위 색상: 높음(빨강), 중간(주황), 낮음(초록)

## 주요 기술 세부사항

- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 34 (Android 14)
- **Kotlin 버전**: 1.9.20
- **Compose Compiler**: 1.5.4
- **KSP**: Room 어노테이션 프로세싱에 사용

## 데이터베이스 스키마

ReminderEntity 필드:
- `id` (Long, 자동 생성)
- `title` (String, 필수)
- `description` (String)
- `dueDateTime` (LocalDateTime?, nullable)
- `priority` (Priority enum: LOW, MEDIUM, HIGH)
- `category` (String)
- `isCompleted` (Boolean)
- `createdAt`, `updatedAt` (LocalDateTime)

## 개발 참고사항

- Room 쿼리는 반응형 UI 업데이트를 위해 `Flow<List<ReminderEntity>>`를 반환
- ViewModel에서 Compose 상태 관리를 위해 StateFlow 사용
- 검색 필터링은 ViewModel의 `getFilteredReminders()`를 통해 메모리 내에서 수행
- 리마인더 완료 토글은 `isCompleted`와 `updatedAt` 필드를 원자적으로 업데이트

## 코딩 규약

### 네이밍 규칙

**클래스와 오브젝트**
- 클래스: `PascalCase` (예: `ReminderEntity`, `ReminderViewModel`)
- 인터페이스: `PascalCase`, 설명적인 이름 (예: `ReminderDao`)
- 오브젝트: `PascalCase` (예: `ReminderDatabase.Companion`)

**함수와 변수**
- 함수: `camelCase`, 동사 접두사 사용 (예: `getReminderById`, `toggleReminderCompletion`)
- 변수: `camelCase` (예: `selectedReminder`, `isCompleted`)
- 상수: `UPPER_SNAKE_CASE` (예: `DATABASE_NAME`)
- Private 프로퍼티: backing properties에는 언더스코어 접두사 (예: MutableStateFlow용 `_selectedReminder`)

**Compose 함수**
- Composable 함수: `PascalCase` (예: `HomeScreen`, `ReminderCard`)
- Composable 파라미터: `camelCase`, 콜백에는 `on` 접두사 (예: `onAddClick`, `onReminderClick`)

**파일명**
- 주요 클래스 이름과 일치 (예: `ReminderEntity.kt`, `HomeScreen.kt`)
- 관련된 작은 클래스들은 적절한 경우 한 파일에 그룹화 (예: `ReminderEntity.kt`의 Priority enum)

### 코드 구조

**패키지 구조**
```
com.reminder/
├── data/
│   ├── entity/      # 데이터 모델과 enum
│   ├── dao/         # Room DAO
│   ├── database/    # 데이터베이스와 컨버터
│   └── repository/  # 데이터 저장소
├── ui/
│   ├── screen/      # 전체 화면
│   ├── components/  # 재사용 가능한 UI 컴포넌트
│   └── theme/       # 테마 관련 파일
└── viewmodel/       # ViewModel과 Factory
```

**파일 내 순서**
1. 패키지 선언
2. Import (Android → Third-party → Java/Kotlin → Internal)
3. 클래스/인터페이스 선언
4. Companion object
5. 프로퍼티 (public → private)
6. Init 블록
7. 생성자
8. Override 함수
9. Public 함수
10. Private 함수

### Kotlin 스타일

**Null 허용 여부**
- 필요할 때 nullable 타입(`?`)을 명시적으로 사용
- `!!`보다 안전 호출(`?.`) 선호 (절대 확실한 경우가 아니면 `!!` 지양)
- 의존성 주입에는 `lateinit`, 선택적 데이터에는 nullable 타입 사용

**불변성**
- `var`보다 `val` 선호
- 업데이트에는 `data class`의 copy 사용 (예: `reminder.copy(isCompleted = true)`)
- 가능한 경우 불변 컬렉션 사용

**Flow와 Coroutines**
- DAO의 모든 데이터베이스 작업은 suspend 함수
- ViewModel 코루틴에는 `viewModelScope` 사용
- Compose 상태 관리를 위해 ViewModel에서 Flow를 StateFlow로 노출
- Flow를 StateFlow로 변환 시 `stateIn()`과 `SharingStarted.WhileSubscribed(5000)` 사용

### Compose 가이드라인

**상태 관리**
- State hoisting: 부모로부터 상태와 콜백을 전달
- UI 상태에는 `remember` 사용 (예: 텍스트 필드 값)
- 비즈니스 로직 상태에는 ViewModel StateFlow 사용
- Composable에서 `collectAsState()`로 StateFlow 수집

**Composable 구조**
```kotlin
@Composable
fun ComponentName(
    data: DataType,              // 데이터 파라미터 먼저
    modifier: Modifier = Modifier,  // 기본값을 가진 Modifier
    onAction: () -> Unit         // 콜백 마지막
) {
    // 구현
}
```

**Modifier 사용**
- 항상 `modifier: Modifier = Modifier` 파라미터 받기
- 받은 modifier를 먼저 적용: `modifier.then(localModifiers)`
- 레이아웃 modifier 전에 의미론적 modifier 사용

### Room Database

**엔티티 설계**
- 항상 `createdAt`과 `updatedAt` 타임스탬프 포함
- ID 필드에는 `@PrimaryKey(autoGenerate = true)` 사용
- 선택적 필드에는 기본값 제공

**DAO 쿼리**
- 반응형 업데이트가 필요한 쿼리는 `Flow<T>` 반환
- 단일 작업(insert/update/delete)은 `suspend fun` 반환
- 명확하고 설명적인 쿼리 함수 이름 사용

### 주석과 문서화

**주석이 필요한 경우**
- 설명이 필요한 복잡한 비즈니스 로직
- 명확하지 않은 Room 쿼리나 Compose 로직
- 컨텍스트가 있는 TODO: `// TODO: 알림 스케줄링 추가`

**주석이 불필요한 경우**
- 자명한 코드
- 코드가 하는 일을 단순히 반복하는 경우
- 주석 처리된 코드 (삭제할 것)

### 아키텍처 규칙

**ViewModel**
- ViewModel에 Context를 절대 전달하지 않기
- Android 프레임워크 의존성 금지 (lifecycle 제외)
- UI에 불변 상태(StateFlow) 노출
- 모든 비즈니스 로직은 여기에 위치

**Repository**
- 데이터 접근의 단일 진실 공급원(Single Source of Truth)
- 데이터 소스 추상화 (Room, network 등)
- 현재 앱에서는 단순 패스스루이지만 확장 가능하도록 준비

**UI Layer**
- 화면은 로컬 UI 상태를 제외하고 무상태(stateless)여야 함
- 직접적인 데이터베이스나 저장소 접근 금지
- 모든 비즈니스 로직은 ViewModel에 위임

**의존성 흐름**
- UI → ViewModel → Repository → DAO → Database
- 레이어를 건너뛰거나 순환 의존성을 만들지 않기
