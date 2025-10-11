# 버그 리포트: BUG_001_CRITICAL

## 버그 ID: BUG_001
**우선순위**: 🔴 **CRITICAL**

---

## 📋 기본 정보

**발견일**: 2025-10-11
**발견자**: Claude (QA 테스터)
**테스트 문서**: FUNCTIONAL_TEST.md
**테스트 케이스**: 앱 시작 (기능 테스트 진행 중)

---

## 🔴 우선순위

선택: [x] Critical

**우선순위 기준**: 앱 크래시 - 앱이 전혀 실행되지 않음

---

## 📝 버그 설명

**요약 (한 줄)**:
앱 시작 시 `NullPointerException: applicationContext must not be null` 발생하여 즉시 크래시

**상세 설명**:
- 앱을 실행하면 `ReminderApplication` 초기화 중에 크래시 발생
- `PreferencesRepository`의 DataStore 초기화 시 `applicationContext`가 null
- 앱이 전혀 사용 불가능한 상태

**영향 범위**:
- 앱 전체 (실행 불가)
- 모든 기능 사용 불가

---

## 🔄 재현 단계

**전제 조건**:
- Android 버전: API 36 (Android 14+)
- 디바이스: Medium_Phone_API_36.1 (에뮬레이터)
- 앱 버전: v1.62.0

**재현 단계**:
1. 에뮬레이터 실행
2. 앱 설치: `adb install app-debug.apk`
3. 앱 실행: `adb shell am start -n com.reminder/.MainActivity`
4. 즉시 크래시 발생

**재현율**: 항상 (100%)

---

## ✅ 예상 결과

앱이 정상적으로 시작되고 홈 화면이 표시되어야 함

---

## ❌ 실제 결과

앱이 즉시 크래시되고 "Application Error: com.reminder" 다이얼로그 표시

---

## 📸 증거 자료

**스크린샷**:
- [ ] 첨부함: `screenshots/02_before_test.png` (크래시 직전)

**로그 파일**:
- [x] 첨부함: `crash_log.txt`

**Logcat 주요 에러**:
```
10-11 18:03:50.161  9205  9205 E AndroidRuntime: FATAL EXCEPTION: main
10-11 18:03:50.161  9205  9205 E AndroidRuntime: Process: com.reminder, PID: 9205
10-11 18:03:50.161  9205  9205 E AndroidRuntime: java.lang.RuntimeException: Unable to instantiate application com.reminder.ReminderApplication package com.reminder
...
10-11 18:03:50.161  9205  9205 E AndroidRuntime: Caused by: java.lang.NullPointerException: applicationContext must not be null
10-11 18:03:50.161  9205  9205 E AndroidRuntime: 	at androidx.datastore.preferences.PreferenceDataStoreSingletonDelegate.getValue(PreferenceDataStoreDelegate.android.kt:106)
10-11 18:03:50.161  9205  9205 E AndroidRuntime: 	at androidx.datastore.preferences.PreferenceDataStoreSingletonDelegate.getValue(PreferenceDataStoreDelegate.android.kt:80)
10-11 18:03:50.161  9205  9205 E AndroidRuntime: 	at com.reminder.data.preferences.PreferencesRepositoryKt.getDataStore(PreferencesRepository.kt:15)
10-11 18:03:50.161  9205  9205 E AndroidRuntime: 	at com.reminder.data.preferences.PreferencesRepositoryKt.access$getDataStore(PreferencesRepository.kt:1)
10-11 18:03:50.161  9205  9205 E AndroidRuntime: 	at com.reminder.data.preferences.PreferencesRepository$Companion.create(PreferencesRepository.kt:176)
10-11 18:03:50.161  9205  9205 E AndroidRuntime: 	at com.reminder.ReminderApplication$attachBaseContext$preferences$1.invokeSuspend(ReminderApplication.kt:135)
```

---

## 🔍 추가 정보

**디바이스 정보**:
- 제조사/모델: Google Emulator (Medium_Phone_API_36.1)
- Android 버전: Android 14+ (API 36)
- 화면 크기: 에뮬레이터 기본
- RAM: 에뮬레이터 기본

**환경 정보**:
- 네트워크: WiFi (에뮬레이터)
- Firebase 로그인 상태: N/A (앱 시작 불가)
- 앱 데이터 크기: 신규 설치

**관련 기능**:
- `ReminderApplication.kt:135` - PreferencesRepository 초기화
- `PreferencesRepository.kt:15` - DataStore 접근
- DataStore 초기화 로직

**비고**:
- 첫 실행 시에는 정상 동작했으나 (3초 시작 시간 측정됨)
- 화면이 꺼진 후 재실행 시 크래시 발생
- 유닛 테스트는 모두 통과했지만 실제 앱 실행에서만 문제 발생

---

## 🛠️ 원인 분석

**원인**:
DataStore 초기화 시점 문제로 추정:
1. `PreferencesRepository.kt:15`에서 DataStore에 접근
2. `preferencesDataStore` delegate가 `applicationContext`를 참조하려 함
3. `ReminderApplication.attachBaseContext` 중에 초기화되는데, 이 시점에 `applicationContext`가 아직 설정되지 않음

**코드 위치**:
```kotlin
// PreferencesRepository.kt:15
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

// ReminderApplication.kt:135
val preferences = runBlocking {
    PreferencesRepository.Companion.create(/* context가 필요함 */)
}
```

---

## 🛠️ 수정 방법

**권장 수정**:

### 방법 1: Lazy 초기화 (권장)
```kotlin
// ReminderApplication.kt
class ReminderApplication : Application() {

    val preferences by lazy {
        PreferencesRepository.create(applicationContext) // lazy로 지연 초기화
    }

    override fun onCreate() {
        super.onCreate()
        // onCreate에서 접근하면 applicationContext가 준비됨
    }
}
```

### 방법 2: onCreate로 이동
```kotlin
// attachBaseContext가 아닌 onCreate에서 초기화
override fun onCreate() {
    super.onCreate()
    preferences = PreferencesRepository.create(applicationContext)
}
```

### 방법 3: DataStore 초기화 방식 변경
```kotlin
// PreferencesRepository.kt
companion object {
    fun create(context: Context): PreferencesRepository {
        val dataStore = context.applicationContext.preferencesDataStore("settings")
        return PreferencesRepository(dataStore)
    }
}
```

**영향 범위**:
- `ReminderApplication.kt` 수정
- `PreferencesRepository.kt` 수정 (선택사항)
- 모든 ViewModel이 preferences 접근 시점 확인 필요

**수정 예상 시간**:
30분 (코드 수정 + 테스트)

---

## ✅ 검증 완료

**수정 버전**: (미수정)
**수정일**: (미수정)
**재검증자**: (미수정)
**재검증 결과**: [ ] PASS / [ ] FAIL

**재검증 단계**:
1. 코드 수정 적용
2. 앱 재빌드: `./gradlew assembleDebug`
3. 앱 재설치: `adb install -r app-debug.apk`
4. 앱 실행: `adb shell am start -n com.reminder/.MainActivity`
5. 크래시 없이 홈 화면 표시 확인
6. 앱 재시작 (홈 버튼 → 최근 앱 → 다시 실행) 테스트

**비고**:
이 버그가 수정되기 전까지는 **모든 기능 테스트 불가능**

---

## 📌 상태 히스토리

| 날짜 | 상태 | 담당자 | 비고 |
|------|------|--------|------|
| 2025-10-11 18:03 | Open | Claude | 버그 발견 및 리포트 작성 |
| 2025-10-11 18:05 | Assigned | ioniere | 수정 필요 |

---

## 📎 관련 링크

- 관련 파일:
  - `app/src/main/java/com/reminder/ReminderApplication.kt:135`
  - `app/src/main/java/com/reminder/data/preferences/PreferencesRepository.kt:15`
- 로그: `QA_TEST/claude/crash_log.txt`

---

## ⚠️ 출시 영향

**출시 가능 여부**: ❌ **즉시 수정 필수**

이 버그로 인해:
- 앱이 전혀 실행되지 않음
- 모든 기능 테스트 불가능
- 출시 절대 불가

**Critical 버그 0개**가 출시 조건이므로, **이 버그를 먼저 수정해야 합니다.**

---

**리포트 작성자**: Claude (AI QA Engineer)
**리포트 작성 시각**: 2025-10-11 18:05 (KST)
