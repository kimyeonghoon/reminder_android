# CLAUDE.md

⚠️ **세션 시작 시 필수**: 이 파일을 먼저 읽고 규칙을 숙지할 것!

---

## 🔥 핵심 규칙 (절대 잊지 말 것!)

### 1. TDD 엄수 ✅
- **항상 테스트 먼저 작성, 구현은 나중**
- Red → Green → Refactor 사이클
- 테스트 없는 코드 = 불완전한 코드

### 2. 세션 시작 워크플로우 🚀
**"다음 작업 진행해줘" 요청 시 자동 실행:**
```bash
./gradlew test           # 1. 테스트 먼저 실행 (필수)
# 테스트 통과 확인 후
# NEXT_TASKS.md 읽고 다음 작업 시작
```

### 3. 민감 정보 절대 커밋 금지 🔒
- API 키, 비밀번호 하드코딩 금지
- `local.properties`에 저장, BuildConfig로 사용
- `.example` 파일에는 placeholder만

### 4. 커밋 규칙 📝
- 형식: `type(scope): 한글 제목`
- 예: `feat(map): 카카오맵 SDK 통합`
- 커밋 전 민감정보 재확인

### 5. MVVM 아키텍처 준수 🏗️
- UI → ViewModel → Repository → DAO → Database
- ViewModel에 Context 전달 금지
- 레이어 건너뛰기 금지

---

## 📊 프로젝트 현황

**현재 버전**: v1.68.0 (versionCode 75, DB v26)
**테스트 커버리지**: 326/326 통과 (100% ✅)

**최근 릴리즈** (v1.64.0~v1.68.0):
- ✅ v1.68.0 - 카카오맵 SDK 통합 (지도 시각화, 위치 선택 UI, MapViewModel TDD)
- ✅ v1.67.1 - 위젯 데이터 표시 버그 수정 (Repository 싱글톤 사용)
- ✅ v1.67.0 - 카카오 로컬 API 장소 검색 (실시간 자동완성, TDD 완료)
- ✅ v1.66.0 - hasTime 필드 추가 (날짜/시간 구분 명시화, 00:00 문제 해결)
- ✅ v1.65.0 - RecurrenceRule UI 재구현 (반복 기능 복원)
- ✅ v1.64.0 - RecurrencePattern 레거시 제거 (646줄 코드 감소)

**주요 완성 기능**: CRUD, Firebase 동기화, 알림, 필터/정렬, 통계, 테마, 위젯, 서브태스크, 이미지 첨부, 백업/복원, 완료 이력, 템플릿, 태그, 배지, 스누즈, 위치 기반 리마인더, TTS, AI 추천, 오프라인, 캘린더 통합, 아카이브, 습관 추적, 포모도로, Eisenhower Matrix, 포커스 모드, 성능 최적화, UI/접근성 개선

---

## 🛠️ 기술 스택

- **언어**: Kotlin 1.9.20
- **UI**: Jetpack Compose (Material 3)
- **아키텍처**: MVVM
- **DB**: Room (v26), WAL mode
- **DI**: 수동 DI (Application 레벨)
- **비동기**: Coroutines + Flow
- **백엔드**: Firebase (Firestore, Auth, FCM, Crashlytics)
- **SDK**: Min 26 (Android 8.0), Target 34 (Android 14)

---

## 📂 패키지 구조

```
com.reminder/
├── data/
│   ├── entity/       # Room Entity, Enum
│   ├── dao/          # DAO (Flow 기반)
│   ├── database/     # Database, Converters
│   └── repository/   # Repository (SSoT)
├── ui/
│   ├── screen/       # 전체 화면 (Compose)
│   ├── components/   # 재사용 컴포넌트
│   └── theme/        # Material 3 테마
├── viewmodel/        # ViewModel + Factory
├── notification/     # 알림, AlarmScheduler
├── location/         # Geofencing, LocationManager
├── ml/               # AI 추천, NLP
├── analytics/        # Analytics, 패턴 분석
├── firebase/         # Firebase 동기화
├── recurrence/       # 반복 규칙 (RecurrenceRule)
├── tts/              # TTS 음성 알림
└── utils/            # 유틸리티
```

---

## ✍️ 코딩 규약 (핵심만)

### 네이밍
- 클래스/인터페이스: `PascalCase`
- 함수/변수: `camelCase` (함수는 동사 시작)
- 상수: `UPPER_SNAKE_CASE`
- Composable: `PascalCase`
- Private backing field: `_propertyName`

### Kotlin 스타일
- `val` > `var`
- `?.` > `!!` (확실할 때만 `!!`)
- `data class` copy() 활용
- 불변 컬렉션 선호

### Compose
- State hoisting 원칙
- `remember` (UI 상태) / `StateFlow` (비즈니스 로직)
- `modifier` 파라미터 항상 제공
- `collectAsState()`로 Flow 수집

### Room
- `Flow<T>` 반환 (반응형 쿼리)
- `suspend fun` (단일 작업)
- `createdAt`, `updatedAt` 타임스탬프 필수
- `@PrimaryKey(autoGenerate = true)`

### 테스트
- **테스트 함수명**: 영어 camelCase + 한글 주석
- **AAA 패턴**: Given → When → Then
- **독립성**: 테스트 간 의존성 없음
- **커버리지 목표**: ViewModel 80%, Repository 70%, DAO 100%

---

## 🔄 Git 워크플로우

### 커밋 메시지
```
type(scope): 한글 제목

body (선택사항, 한글)
```

**타입**: `feat`, `fix`, `refactor`, `test`, `docs`, `chore`, `perf`
**Scope**: `ui`, `database`, `viewmodel`, `map`, `location` 등

### 릴리즈
1. `build.gradle.kts`에서 `versionCode`, `versionName` 업데이트
2. `CLAUDE.md`, `NEXT_TASKS.md` 업데이트
3. 커밋 및 Push
4. GitHub Release 생성 (태그: `v1.x.x`)

---

## 🚫 주의사항

### 절대 하지 말 것
- ❌ TDD 없이 구현
- ❌ API 키 하드코딩
- ❌ ViewModel에 Context 전달
- ❌ 레이어 건너뛰기 (UI → DAO 직접 접근 등)
- ❌ 테스트 없이 커밋
- ❌ `--no-verify`로 hook 우회

### 항상 할 것
- ✅ 테스트 먼저 작성
- ✅ local.properties에 민감정보 저장
- ✅ MVVM 아키텍처 준수
- ✅ Flow/StateFlow 활용
- ✅ 커밋 전 민감정보 확인
- ✅ 한글 커밋 메시지

---

## 📋 자주 쓰는 명령어

```bash
# 빌드 & 테스트
./gradlew test                    # 유닛 테스트
./gradlew connectedAndroidTest    # 계측 테스트 (에뮬레이터 필요)
./gradlew build                   # 빌드
./gradlew clean                   # 클린 빌드

# 설치
./gradlew installDebug            # 디바이스/에뮬레이터 설치

# Git
git add .
git commit -m "type(scope): 제목"
git push origin main
```

---

## 🎯 다음 작업

**NEXT_TASKS.md 참고**

---

**Happy Coding! 🚀**

_v1.68.0 완료 - 75개 버전, 26개 DB 마이그레이션, 326개 테스트 100% 통과_
