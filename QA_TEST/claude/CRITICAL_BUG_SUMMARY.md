# 🚨 Critical 버그 발견 - 즉시 수정 필요!

**발견 시각**: 2025-10-11 18:05 (KST)
**테스트 진행**: 기능 테스트 중 앱 크래시 발견
**영향도**: ❌ **앱 실행 불가 - 출시 차단**

---

## 📋 버그 요약

**버그 ID**: BUG_001_CRITICAL
**증상**: 앱 실행 즉시 크래시 (Application Error)
**원인**: `ReminderApplication.attachBaseContext`에서 DataStore 조기 접근
**파일**: `ReminderApplication.kt:134-136`

---

## 🔍 기술적 원인

### 문제 코드
```kotlin
// ReminderApplication.kt:127-141
override fun attachBaseContext(base: Context?) {
    if (base == null) {
        super.attachBaseContext(base)
        return
    }

    // DataStore에서 언어 설정 읽기 (동기적으로)
    val preferences = runBlocking {
        PreferencesRepository.create(base).userPreferences.first() // ← 크래시 발생!
    }

    // 저장된 언어로 Context 업데이트
    val updatedContext = LocaleHelper.updateLocale(base, preferences.language)
    super.attachBaseContext(updatedContext)
}
```

### 크래시 스택트레이스
```
java.lang.NullPointerException: applicationContext must not be null
at androidx.datastore.preferences.PreferenceDataStoreSingletonDelegate.getValue
at com.reminder.data.preferences.PreferencesRepositoryKt.getDataStore:15
at com.reminder.ReminderApplication$attachBaseContext$preferences$1.invokeSuspend:135
```

### 왜 문제인가?
1. `attachBaseContext`는 `Application` 초기화의 **첫 단계**
2. 이 시점에 `applicationContext`는 아직 **null**
3. DataStore delegate(`preferencesDataStore`)는 내부적으로 `applicationContext` 필요
4. → NullPointerException 발생

---

## 💡 해결 방법 (3가지)

### ✅ 방법 1: SharedPreferences로 변경 (빠르고 안전)

**장점**: 간단하고 확실한 수정
**단점**: 언어 설정만 SharedPreferences 사용

```kotlin
// ReminderApplication.kt
override fun attachBaseContext(base: Context?) {
    if (base == null) {
        super.attachBaseContext(base)
        return
    }

    // SharedPreferences로 언어 설정 읽기 (DataStore 대신)
    val prefs = base.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
    val languageCode = prefs.getString("language", Language.SYSTEM.code) ?: Language.SYSTEM.code
    val language = Language.fromCode(languageCode)

    // 저장된 언어로 Context 업데이트
    val updatedContext = LocaleHelper.updateLocale(base, language)
    super.attachBaseContext(updatedContext)
}

// PreferencesRepository.kt - updateLanguage 메서드 수정
suspend fun updateLanguage(language: Language) {
    dataStore.edit { preferences ->
        preferences[PreferencesKeys.LANGUAGE] = language.code
    }

    // SharedPreferences에도 저장 (attachBaseContext에서 읽기 위해)
    // context는 생성자에서 받거나 필드로 저장 필요
    // 대안: 별도 함수로 분리
}
```

**추가 작업 필요**: PreferencesRepository에 Context 저장 필요

---

### ✅ 방법 2: onCreate로 언어 설정 이동 (권장!)

**장점**: 코드 변경 최소화, DataStore 그대로 사용
**단점**: 첫 실행 시 잠깐 시스템 언어로 표시될 수 있음

```kotlin
// ReminderApplication.kt
override fun attachBaseContext(base: Context?) {
    super.attachBaseContext(base)  // 그냥 base 그대로 전달
}

override fun onCreate() {
    super.onCreate()

    // ★ 언어 설정을 onCreate에서 적용
    applicationScope.launch {
        val preferences = preferencesRepository.userPreferences.first()

        // 언어가 시스템 기본이 아니면 Activity 재생성으로 언어 적용
        if (preferences.language != Language.SYSTEM) {
            LocaleHelper.setAppLanguage(this@ReminderApplication, preferences.language)
            // 또는 다음 Activity 시작 시 적용되도록 설정
        }
    }

    // Firebase Crashlytics 초기화
    setupCrashlytics()
    // ... 나머지 코드
}
```

**참고**: `LocaleHelper`에 `setAppLanguage` 메서드 추가 필요 (Configuration 업데이트)

---

### ⚠️ 방법 3: DataStore 직접 초기화 (고급)

**장점**: 완벽한 제어
**단점**: 복잡하고 에러 가능성 높음

```kotlin
// PreferencesRepository.kt - Companion에 별도 메서드 추가
companion object {
    fun create(context: Context): PreferencesRepository {
        return PreferencesRepository(context.dataStore)
    }

    // attachBaseContext용 안전한 생성 메서드
    fun createSafe(context: Context): PreferencesRepository? {
        return try {
            // DataStore 직접 생성 (delegate 사용 안 함)
            val dataStore = PreferenceDataStoreFactory.create(
                produceFile = { context.filesDir.resolve("datastore/user_preferences.preferences_pb") }
            )
            PreferencesRepository(dataStore)
        } catch (e: Exception) {
            null  // 실패 시 null 반환
        }
    }
}

// ReminderApplication.kt
override fun attachBaseContext(base: Context?) {
    if (base == null) {
        super.attachBaseContext(base)
        return
    }

    val language = try {
        runBlocking {
            PreferencesRepository.createSafe(base)?.userPreferences?.first()?.language
                ?: Language.SYSTEM
        }
    } catch (e: Exception) {
        Language.SYSTEM  // 실패 시 시스템 기본 언어
    }

    val updatedContext = LocaleHelper.updateLocale(base, language)
    super.attachBaseContext(updatedContext)
}
```

---

## 🎯 **권장: 방법 2 (onCreate 이동)**

**이유**:
1. ✅ 코드 변경 최소
2. ✅ DataStore 그대로 사용 (일관성 유지)
3. ✅ 안전한 수정
4. ⚠️ 단점 미미 (첫 화면만 잠깐 시스템 언어)

---

## 🔧 즉시 수정 코드 (방법 2)

아래 코드를 복사해서 적용하세요:

```kotlin
// ReminderApplication.kt - attachBaseContext 수정
override fun attachBaseContext(base: Context?) {
    // DataStore 사용하지 말고 일단 base 그대로 전달
    super.attachBaseContext(base)
}

// onCreate에 언어 설정 추가
override fun onCreate() {
    super.onCreate()

    // Firebase Crashlytics 초기화
    setupCrashlytics()

    // ★ 언어 설정 적용 (DataStore가 안전하게 사용 가능한 시점)
    applicationScope.launch {
        try {
            val preferences = preferencesRepository.userPreferences.first()
            // TODO: 필요 시 LocaleHelper로 언어 적용
            // 현재는 Activity에서 적용되므로 여기서는 로깅만
            Log.d("ReminderApp", "Loaded language: ${preferences.language}")
        } catch (e: Exception) {
            Log.e("ReminderApp", "Failed to load preferences", e)
        }
    }

    // v1.29.0: 모든 알림 채널 생성
    notificationHelper.createAllNotificationChannels()

    // ... 나머지 코드 그대로
}
```

---

## ✅ 수정 후 테스트 절차

1. **코드 수정**
   ```kotlin
   // attachBaseContext에서 DataStore 제거
   // onCreate에서 언어 설정 로드 (위 코드 참고)
   ```

2. **재빌드**
   ```bash
   ./gradlew clean assembleDebug
   ```

3. **재설치**
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

4. **테스트**
   ```bash
   adb shell am start -n com.reminder/.MainActivity
   # 크래시 없이 실행되는지 확인
   ```

5. **로그 확인**
   ```bash
   adb logcat | grep -i "reminder\|crash"
   # 에러 없는지 확인
   ```

---

## 📊 수정 후 예상 결과

### ✅ 성공 시
- 앱이 정상 시작 (3초 이내)
- 크래시 없음
- 홈 화면 표시
- 기능 테스트 진행 가능

### 🎯 출시 가능성
- Critical 버그 → 0개
- High 버그 → 0개
- **출시 가능!** 🚀

---

## 📝 다음 단계

**지금 당장**:
1. 위 코드 수정 (방법 2 권장)
2. 재빌드 및 재설치
3. 앱 실행 확인
4. Claude에게 "수정했어, 다시 테스트해줘" 요청

**수정 완료 후**:
- 기능 테스트 재개 (FUNCTIONAL_TEST.md)
- 5-10분이면 기본 CRUD 테스트 완료
- 최종 출시 결정

---

**작성자**: Claude (AI QA Engineer)
**긴급도**: 🔴 CRITICAL - 즉시 수정 필요
**예상 수정 시간**: 10분
