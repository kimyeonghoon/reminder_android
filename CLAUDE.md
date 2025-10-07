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

### 테스트 주도 개발 (TDD)

**TDD 사이클 (필수)**
1. **Red** - 실패하는 테스트를 먼저 작성
2. **Green** - 테스트를 통과하는 최소한의 코드 작성
3. **Refactor** - 코드 품질 개선 (테스트는 여전히 통과)

**중요: 항상 테스트를 먼저 작성한다!**

**테스트 종류**

**1. Unit Tests (유닛 테스트)**
- 위치: `app/src/test/java/`
- 대상: ViewModel, Repository, 비즈니스 로직
- 프레임워크: JUnit4/5, Mockito, MockK
- 실행: JVM에서 빠르게 실행

```kotlin
// 예시: ReminderViewModelTest.kt
@Test
fun `리마인더 추가 시 목록에 반영된다`() {
    // Given
    val title = "테스트 할일"

    // When
    viewModel.addReminder(title)

    // Then
    val reminders = viewModel.allReminders.value
    assertTrue(reminders.any { it.title == title })
}
```

**2. Integration Tests (통합 테스트)**
- 위치: `app/src/androidTest/java/`
- 대상: Room Database, Repository와 DAO 통합
- 프레임워크: AndroidJUnit4, Room Testing
- 실행: Android 디바이스/에뮬레이터 필요

```kotlin
// 예시: ReminderDaoTest.kt
@Test
fun insertAndGetReminder() = runTest {
    // Given
    val reminder = ReminderEntity(title = "테스트")

    // When
    dao.insertReminder(reminder)

    // Then
    val loaded = dao.getAllReminders().first()
    assertEquals(1, loaded.size)
    assertEquals("테스트", loaded[0].title)
}
```

**3. UI Tests (Compose 테스트)**
- 위치: `app/src/androidTest/java/`
- 대상: Composable 함수, 화면 상호작용
- 프레임워크: Compose Testing, Espresso
- 실행: Android 디바이스/에뮬레이터 필요

```kotlin
// 예시: HomeScreenTest.kt
@Test
fun addButton_whenClicked_navigatesToAddScreen() {
    composeTestRule.setContent {
        HomeScreen(viewModel, onAddClick = { navigated = true })
    }

    composeTestRule.onNodeWithContentDescription("Add Reminder").performClick()

    assertTrue(navigated)
}
```

**테스트 작성 규칙**

1. **테스트 이름**: 한글로 작성 가능, 명확하게
   - `fun 리마인더_추가_시_목록에_반영된다()`
   - 또는 백틱 사용: `` `리마인더 추가 시 목록에 반영된다` ``

2. **AAA 패턴 사용**
   - **Arrange** (Given): 테스트 준비
   - **Act** (When): 실행
   - **Assert** (Then): 검증

3. **테스트는 독립적**
   - 다른 테스트에 의존하지 않음
   - 실행 순서에 무관하게 통과

4. **테스트는 빠르게**
   - Unit Test는 1초 이내
   - 느린 테스트는 통합 테스트로 분리

**테스트 커버리지 목표**
- ViewModel: 80% 이상
- Repository: 70% 이상
- DAO: 주요 쿼리 100%
- UI: 주요 사용자 시나리오

**TDD 워크플로우 예시**

```bash
# 1. 실패하는 테스트 작성
# ReminderViewModelTest.kt에 새 테스트 추가

# 2. 테스트 실행 (실패 확인)
./gradlew test

# 3. 최소 코드 구현
# ReminderViewModel.kt에 기능 추가

# 4. 테스트 실행 (통과 확인)
./gradlew test

# 5. 리팩토링
# 코드 개선, 테스트는 여전히 통과

# 6. 커밋
git commit -m "feat(viewmodel): 리마인더 추가 기능 구현"
```

**Mock 사용**
- Repository를 테스트할 때는 DAO를 Mock
- ViewModel을 테스트할 때는 Repository를 Mock
- 실제 객체는 통합 테스트에서만 사용

**테스트 도구**
```kotlin
// build.gradle.kts에 이미 포함됨
testImplementation("junit:junit:4.13.2")
testImplementation("org.mockito:mockito-core:5.7.0")
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
androidTestImplementation("androidx.compose.ui:ui-test-junit4")
```

**중요: Claude Code 작업 시**
- 새 기능 구현 요청 시, 테스트부터 작성
- "테스트 먼저, 구현 나중에" 원칙 엄수
- 테스트 없는 코드는 불완전한 코드

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

## Git 워크플로우

### 브랜치 전략 (GitHub Flow)

**메인 브랜치**
- `main` - 항상 배포 가능한 상태 유지, 태그로 버전 관리

**작업 브랜치**
- `feature/기능명` - 새로운 기능 개발
- `fix/버그명` - 버그 수정
- `refactor/내용` - 리팩토링
- `docs/내용` - 문서 작업

**작업 흐름**
1. `main`에서 작업 브랜치 생성
2. 작업 완료 후 Pull Request 생성
3. 리뷰 및 테스트
4. `main`에 병합
5. 작업 브랜치 삭제

### 커밋 메시지 규칙 (Conventional Commits)

**형식**
```
type(scope): subject

body (선택사항)
```

**타입**
- `feat`: 새로운 기능 추가
- `fix`: 버그 수정
- `refactor`: 코드 리팩토링 (기능 변경 없음)
- `style`: 코드 포맷팅, 세미콜론 누락 등
- `docs`: 문서 수정
- `test`: 테스트 코드 추가/수정
- `chore`: 빌드 스크립트, 패키지 매니저 설정 등
- `perf`: 성능 개선

**Scope (선택사항)**
- `ui`: UI 관련
- `database`: 데이터베이스 관련
- `viewmodel`: ViewModel 관련
- `repository`: Repository 관련

**예시**
```
feat(ui): 리마인더 날짜 선택기 추가
fix(database): null 값 반환 오류 수정
refactor(viewmodel): 상태 관리 로직 단순화
docs: README에 빌드 방법 추가
chore: Gradle 8.4로 업그레이드
```

**규칙**
- type과 scope는 영어로 유지
- subject는 한글로 작성, 50자 이내로 간결하게
- subject 끝에 마침표 생략
- body는 선택사항, 한글로 작성
- body에는 "무엇을", "왜" 변경했는지 작성

### 버전 관리 (Semantic Versioning)

**버전 형식**: `MAJOR.MINOR.PATCH` (예: `1.2.3`)

- **MAJOR**: 호환되지 않는 API 변경
- **MINOR**: 하위 호환성을 유지하는 기능 추가
- **PATCH**: 하위 호환성을 유지하는 버그 수정

**versionCode vs versionName**
- `versionCode`: 정수, 매 릴리즈마다 증가 (1, 2, 3, ...)
- `versionName`: 문자열, Semantic Versioning ("1.0.0", "1.1.0", ...)

### 릴리즈 프로세스

1. 버전 번호 결정 (Semantic Versioning 기준)
2. `app/build.gradle.kts`에서 `versionCode`와 `versionName` 업데이트
3. `CHANGELOG.md` 업데이트
4. 커밋 및 Push
5. GitHub에서 Release 생성
   - 태그: `v1.0.0` 형식
   - Release notes: CHANGELOG 내용 복사
   - APK 파일 첨부
6. 태그 생성 및 Push

### Pull Request 가이드

**PR 제목**
- 커밋 메시지 규칙과 동일 (`feat(ui): 알림 기능 추가`)

**PR 설명에 포함할 내용**
- 변경 사항 요약
- 변경 이유
- 테스트 방법
- 스크린샷 (UI 변경 시)
- 관련 이슈 번호

**리뷰 규칙**
- 최소 1명의 승인 필요 (팀 프로젝트 시)
- 빌드 성공 확인
- 충돌 해결 후 병합

### Issue 관리

**레이블**
- `bug`: 버그 리포트
- `enhancement`: 기능 개선 제안
- `feature`: 새로운 기능 요청
- `documentation`: 문서 관련
- `good first issue`: 초보자 친화적인 이슈
- `help wanted`: 도움 필요

**Milestone**
- 버전별로 milestone 생성 (v1.0.0, v1.1.0 등)
- 해당 버전에 포함될 이슈들을 milestone에 할당

## 민감 정보 관리

### 절대 커밋하지 말아야 할 것들

**파일**
- `*.keystore`, `*.jks` - 앱 서명 키
- `key.properties` - 서명 설정
- `google-services.json` - Firebase 설정 (프로덕션)
- `local.properties` - SDK 경로 및 로컬 설정
- `.env` 파일 - 환경 변수
- `secrets.xml` - 민감한 리소스

**코드 내 하드코딩 금지**
- API 키 (Google Maps, Weather API 등)
- 비밀번호, 토큰
- 서버 URL (프로덕션)
- OAuth 클라이언트 시크릿
- 데이터베이스 연결 정보

### 민감 정보 관리 방법

**1. local.properties 사용 (권장)**
```properties
# local.properties
MAPS_API_KEY=your_actual_api_key_here
API_BASE_URL=https://api.example.com
```

```kotlin
// build.gradle.kts에서 읽기
val localProperties = Properties()
localProperties.load(FileInputStream(rootProject.file("local.properties")))

android {
    defaultConfig {
        buildConfigField("String", "MAPS_API_KEY",
            "\"${localProperties.getProperty("MAPS_API_KEY")}\"")
    }
}
```

**2. BuildConfig 활용**
```kotlin
// 코드에서 사용
val apiKey = BuildConfig.MAPS_API_KEY
```

**3. 환경 변수 사용**
```kotlin
// build.gradle.kts
buildConfigField("String", "API_KEY", "\"${System.getenv("API_KEY")}\"")
```

**4. strings.xml 분리 (비공개 리소스)**
```xml
<!-- app/src/main/res/values/secrets.xml (git-ignored) -->
<resources>
    <string name="api_key">your_api_key</string>
</resources>
```

### 예시 파일 제공

프로젝트에는 `.example` 파일을 포함:
- `local.properties.example` - 로컬 설정 예시
- `key.properties.example` - 서명 설정 예시 (미래)
- `secrets.xml.example` - 리소스 예시 (미래)

개발자는 `.example` 파일을 복사하여 실제 값으로 채워야 합니다.

### Pre-commit Hook

프로젝트에는 민감 정보 커밋을 방지하는 pre-commit hook이 설정되어 있습니다:
- `.git/hooks/pre-commit`
- 민감한 파일 및 패턴 자동 검사
- 감지 시 커밋 차단

### Claude Code 작업 시 주의사항

**절대 하지 말 것:**
- 실제 API 키를 코드에 하드코딩
- `.example` 파일에 실제 값 입력
- 민감한 파일을 git add
- `--no-verify` 플래그로 hook 우회

**항상 할 것:**
- 민감 정보는 local.properties에 저장
- 예시 파일에는 placeholder만 사용
- 커밋 전 민감 정보 재확인
- README에 설정 방법 문서화
