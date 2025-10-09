# 다음 세션 작업 계획

> 이 파일은 세션 시작 시 읽고 삭제하세요.

## 📊 현재 상태 (2025-10-09)

### ✅ 완료된 버전
- **v1.15.0**: 최종 폴리싱 (알림 커스터마이징, 문서화)
- **v1.16.0**: 기술 부채 해결 (라이브러리 업데이트, 코드 리팩토링)

### 📌 현재 브랜치
- `main` 브랜치 (v1.16.0 태그까지 완료)

---

## 🎯 다음 작업: v1.17.0 - 분석 & 모니터링

### 구현할 기능

#### 1. Firebase Analytics 이벤트 추가
**위치**: `app/src/main/java/com/reminder/analytics/`

이벤트 추적 항목:
- **리마인더 작업**
  - `reminder_created`: 리마인더 생성 (priority, category, hasRecurrence)
  - `reminder_completed`: 리마인더 완료 (daysUntilDue)
  - `reminder_deleted`: 리마인더 삭제
  - `reminder_edited`: 리마인더 수정

- **고급 기능 사용**
  - `subtask_added`: 서브태스크 추가
  - `image_attached`: 이미지 첨부
  - `template_created`: 템플릿 생성
  - `template_used`: 템플릿 사용
  - `batch_operation`: 배치 작업 (operation_type, count)

- **검색 & 필터**
  - `search_performed`: 검색 수행 (query_length)
  - `filter_applied`: 필터 적용 (filter_type)
  - `sort_changed`: 정렬 변경 (sort_option)

- **설정 변경**
  - `theme_changed`: 테마 변경
  - `notification_settings_changed`: 알림 설정 변경
  - `simple_mode_toggled`: 간편 모드 전환

#### 2. AnalyticsHelper 클래스 생성
```kotlin
// app/src/main/java/com/reminder/analytics/AnalyticsHelper.kt
class AnalyticsHelper(private val firebaseAnalytics: FirebaseAnalytics) {
    fun logReminderCreated(priority: Priority, category: String, hasRecurrence: Boolean)
    fun logReminderCompleted(daysUntilDue: Int?)
    fun logSearchPerformed(queryLength: Int)
    // ... 기타 메서드
}
```

#### 3. ViewModel에 Analytics 통합
- ReminderViewModel에 AnalyticsHelper 주입
- 주요 액션에 이벤트 로깅 추가
- SettingsViewModel에도 통합

#### 4. Crashlytics 개선
- 사용자 속성 설정 (preferredTheme, simpleMode, totalReminders)
- 주요 작업에 breadcrumb 추가
- 커스텀 키 추가 (lastAction, screenName)

#### 5. 사용자 속성 (User Properties)
```kotlin
// 설정
firebaseAnalytics.setUserProperty("theme_mode", themeMode.name)
firebaseAnalytics.setUserProperty("simple_mode", simpleMode.toString())
firebaseAnalytics.setUserProperty("total_reminders", count.toString())
```

---

## 🎨 v1.18.0 - UX/UI 개선

### 구현할 기능

#### 1. 애니메이션 추가
- **리스트 아이템**
  - 추가/삭제 시 Fade + Scale 애니메이션
  - 완료 체크 시 체크 애니메이션

- **화면 전환**
  - Navigation Compose 전환 애니메이션
  - Slide + Fade 효과

- **FAB 애니메이션**
  - Extended FAB (스크롤 시 축소/확장)
  - Rotation 애니메이션

#### 2. 테마 색상 옵션 확장
- 프리셋 색상 테마 추가 (파랑, 초록, 보라, 핑크)
- 커스텀 컬러 피커 (선택 사항)

#### 3. 추가 접근성 개선
- 햅틱 피드백 추가
- 포커스 인디케이터 개선
- 고대비 모드 지원

#### 4. 온보딩 개선
- Pager 기반 온보딩 재구성
- Lottie 애니메이션 추가 (선택 사항)

---

## ✨ v1.19.0 - 추가 기능

### 구현할 기능

#### 1. 고급 검색
**위치**: `app/src/main/java/com/reminder/ui/screen/SearchScreen.kt`

- 날짜 범위 검색
- 다중 필터 조합 (우선순위 + 카테고리)
- 태그 기반 검색
- 최근 검색 기록

#### 2. 위치 기반 알림 (선택 사항)
- Geofencing API 사용
- 특정 장소 도착 시 알림

#### 3. 위젯 확장
- 다양한 크기 (2x2, 4x2, 4x4)
- 다양한 스타일 (미니멀, 상세)
- 위젯 설정 화면

#### 4. 음성 명령 통합
- Google Assistant Actions
- "Hey Google, 할 일 추가" 지원

---

## 📝 작업 순서

1. **v1.17.0 브랜치 생성**
   ```bash
   git checkout -b feature/v1.17.0-analytics
   ```

2. **AnalyticsHelper 구현 (TDD)**
   - 테스트 먼저 작성
   - 클래스 구현
   - ViewModel 통합

3. **이벤트 로깅 추가**
   - ReminderViewModel
   - SettingsViewModel
   - UI 스크린들

4. **Crashlytics 개선**
   - 사용자 속성 설정
   - Breadcrumb 추가

5. **테스트 & 릴리즈**
   - 빌드 테스트
   - main 브랜치 merge
   - v1.17.0 태그 생성

---

## 🔧 기술 스택 참고

### Firebase Analytics
```gradle
// 이미 포함됨
implementation("com.google.firebase:firebase-analytics-ktx")
```

### 사용 예시
```kotlin
val bundle = Bundle().apply {
    putString("priority", priority.name)
    putString("category", category)
    putBoolean("has_recurrence", hasRecurrence)
}
firebaseAnalytics.logEvent("reminder_created", bundle)
```

---

## 📚 참고 문서

- [Firebase Analytics 이벤트](https://firebase.google.com/docs/analytics/events)
- [Crashlytics Custom Keys](https://firebase.google.com/docs/crashlytics/customize-crash-reports)
- [Jetpack Compose Animation](https://developer.android.com/jetpack/compose/animation)

---

## ⚠️ 주의사항

1. **TDD 엄수**: 모든 새 기능은 테스트 먼저 작성
2. **CLAUDE.md 규칙 준수**: 한글 커밋 메시지, MVVM 아키텍처
3. **민감 정보**: 실제 사용자 데이터를 Analytics에 포함하지 않기
4. **성능**: 과도한 이벤트 로깅 주의 (배터리 소모)

---

## 💡 팁

- Analytics 이벤트는 실시간으로 Firebase Console에서 확인 가능
- 디버그 모드에서 테스트: `adb shell setprop debug.firebase.analytics.app com.reminder`
- Crashlytics 테스트: 의도적으로 크래시 발생 후 확인

---

**작성일**: 2025-10-09
**마지막 커밋**: v1.16.0 (main 브랜치)
**다음 버전**: v1.17.0 - 분석 & 모니터링
