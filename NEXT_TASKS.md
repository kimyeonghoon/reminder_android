# 다음 작업 계획

> 마지막 업데이트: 2025-10-10 (v1.27.1)

## 📋 우선순위별 작업 목록

### 🔥 높음 (바로 시작 가능)

#### 1. Git 상태 정리 및 커밋
**현재 상태**:
```
D .claude/README.md
D .claude/hooks/session_start.sh
D .claude/settings.json
?? .claude/settings.local.json
```

**작업 내용**:
- `.claude/settings.local.json` 파일 확인 (git-ignored 로컬 설정)
- 삭제된 파일들 처리
- 변경사항 커밋: `chore(claude): Claude Code 설정 업데이트`

**예상 시간**: 5분

---

#### 2. Firebase 완료 항목 삭제 로직 구현 (TODO 해결)
**위치**: `app/src/main/java/com/reminder/data/repository/FirebaseSyncRepository.kt:99`

**TODO 내용**:
```kotlin
// TODO: 완료된 항목들을 원격에서도 삭제하는 로직 필요
```

**작업 순서** (TDD):
1. **테스트 작성** (`FirebaseSyncRepositoryTest.kt`):
   ```kotlin
   @Test
   fun `완료된 리마인더 삭제 시 Firestore에서도 삭제된다`() {
       // Given: 완료된 리마인더
       // When: deleteCompleted() 호출
       // Then: Firestore에서 해당 문서 삭제 확인
   }
   ```

2. **구현**:
   - `FirebaseSyncRepository`에 `deleteCompletedFromFirestore()` 메서드 추가
   - `FirestoreDataSource`에 `deleteReminder(id)` 메서드 추가
   - 에러 핸들링 추가 (네트워크 오류 시 무시)

3. **테스트 실행**: `./gradlew test`

**예상 시간**: 30분

---

### ⚡ 중간 (다음 버전 준비)

#### 3. v1.28.0 신규 기능 계획

**옵션 A: 푸시 알림 고도화** 🔔
- FCM (Firebase Cloud Messaging) 통합
- 알림 채널 세분화 (우선순위별, 카테고리별)
- 리치 알림 (이미지, 액션 버튼)
- **DB 변경**: 없음
- **예상 시간**: 2-3시간

**옵션 B: 차트 시각화 강화** 📊
- MPAndroidChart 활용 (이미 의존성 있음)
- 주간/월간 트렌드 그래프
- 카테고리별 완료율 파이 차트
- **DB 변경**: 없음
- **예상 시간**: 2시간

**옵션 C: 다국어 지원 (i18n)** 🌍
- strings.xml 리소스 분리 (ko, en, zh)
- Locale 감지 및 자동 변경
- 70대 사용자 고려 (한/영 토글 버튼)
- **DB 변경**: 없음
- **예상 시간**: 3-4시간

**옵션 D: 커스텀 테마** 🎨
- 사용자 정의 색상 선택
- 미리보기 기능
- DataStore에 테마 설정 저장
- **DB 변경**: 없음
- **예상 시간**: 2시간

**추천**: **옵션 B (차트 강화)** - 이미 MPAndroidChart 의존성이 있고, 통계 기능 확장에 자연스러움

---

#### 4. UI 테스트 커버리지 확대

**아직 테스트 없는 화면**:
- [ ] `StatisticsScreen.kt` (통계 대시보드)
- [ ] `CompletionHistoryScreen.kt` (완료 이력 달력)
- [ ] `SettingsScreen.kt` (설정 화면)
- [ ] `HelpScreen.kt` (도움말)

**작업 순서**:
1. `StatisticsScreenTest.kt` 작성 (15-20개 테스트)
   - 화면 표시 확인
   - 차트 데이터 렌더링
   - 필터 동작
   - 패턴 분석 버튼 네비게이션

2. `SettingsScreenTest.kt` 작성 (10-15개 테스트)
   - 테마 토글
   - 글씨 크기 변경
   - 간편 모드 토글
   - Firebase 로그인/로그아웃

**예상 시간**: 각 1시간

---

### 🔵 낮음 (유지보수)

#### 5. 문서 업데이트
- [ ] `CLAUDE.md` 프로젝트 현황을 v1.27.1로 업데이트
- [ ] `README.md`에 v1.27.1 릴리즈 노트 추가 (이미 CHANGELOG.md에는 있음)
- [ ] API 문서 생성 (KDoc → Dokka)

**예상 시간**: 30분

---

## 🚀 빠른 시작 가이드

### 다음 세션 시작 시:

1. **즉시 시작하려면**:
   ```
   "1번 Git 정리부터 시작해줘"
   또는
   "2번 TODO 해결해줘 (TDD로)"
   ```

2. **새 기능 개발하려면**:
   ```
   "v1.28.0으로 차트 강화 기능 구현해줘"
   또는
   "다국어 지원 추가해줘"
   ```

3. **테스트 작성하려면**:
   ```
   "StatisticsScreen UI 테스트 작성해줘"
   ```

---

## 📝 참고사항

- **TDD 필수**: 모든 새 기능은 테스트 먼저 작성
- **커밋 메시지**: `type(scope): 한글 제목` 형식
- **버전 업데이트**: `app/build.gradle.kts`에서 versionCode/versionName 수정
- **문서화**: CHANGELOG.md, CLAUDE.md, README.md 동기화

---

## 🔗 관련 파일

- 프로젝트 가이드: `CLAUDE.md`
- 변경 이력: `CHANGELOG.md`
- 빌드 설정: `app/build.gradle.kts`
- TODO 위치: `app/src/main/java/com/reminder/data/repository/FirebaseSyncRepository.kt:99`

---

**이 파일을 다 읽었으면, 위 작업 중 하나를 선택해서 시작하세요! 🚀**
