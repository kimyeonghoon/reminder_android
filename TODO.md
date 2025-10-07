# TODO - Reminder 앱 개발 현황

## 📌 현재 상태 (2025-10-07)

### 릴리즈 버전
- **v1.0.0** (main 브랜치) - 기본 CRUD 기능 완성

### 브랜치 구조
```
main (v1.0.0) - 안정 버전
├── feature/datetime-picker     ⏰ 날짜/시간 선택 UI
├── feature/notification        🔔 알림/푸시 기능
├── feature/firebase-sync       🔄 Firebase 실시간 동기화
├── feature/filter-sort         🗂️ 필터링 & 정렬
├── feature/statistics          📊 통계/대시보드
└── feature/theme-settings      🎨 테마/설정
```

---

## ✅ 완료된 기능 (v1.0.0)

- [x] MVVM 아키텍처 구현
- [x] Room Database 로컬 저장
- [x] 리마인더 CRUD (생성/조회/수정/삭제)
- [x] 우선순위 시스템 (LOW/MEDIUM/HIGH)
- [x] 카테고리 기능
- [x] 검색 기능 (제목/설명/카테고리)
- [x] 완료/미완료 토글
- [x] Material 3 디자인
- [x] ViewModel 단위 테스트 (100%)
- [x] Repository 단위 테스트 (100%)
- [x] DAO 통합 테스트 (100%)

---

## 🚧 진행 예정 기능

### 1. 🗓️ 날짜/시간 선택 UI (feature/datetime-picker)
**우선순위: 높음**
- [ ] DatePicker 추가
- [ ] TimePicker 추가
- [ ] AddEditReminderScreen에 UI 추가
- [ ] dueDateTime 필드 활용
- [ ] 테스트 작성
- [ ] main 머지 후 v1.1.0 릴리즈

**이유:** dueDateTime 필드는 있지만 UI가 없어서 설정 불가

---

### 2. 🔔 알림/푸시 기능 (feature/notification)
**우선순위: 높음**
- [ ] NotificationManager 구현
- [ ] AlarmManager/WorkManager 선택
- [ ] BroadcastReceiver 추가
- [ ] NotificationChannel 설정
- [ ] 권한 요청 (POST_NOTIFICATIONS)
- [ ] 반복 알림 옵션
- [ ] 스누즈 기능
- [ ] 테스트 작성
- [ ] main 머지 후 v1.2.0 릴리즈

**기술 선택:**
- 로컬 알림: AlarmManager (정확한 시간) 또는 WorkManager (안정성)
- 푸시 알림: Firebase Cloud Messaging (나중에 추가)

---

### 3. 🔄 Firebase 실시간 동기화 (feature/firebase-sync)
**우선순위: 중간** (테스트 단계)
- [ ] Firebase 의존성 추가
- [ ] Firestore 설정
- [ ] Firebase Auth 구현
- [ ] Repository에 Remote 데이터 소스 추가
- [ ] SyncManager 구현
  - [ ] Offline-first 전략
  - [ ] 로컬 우선 읽기
  - [ ] 백그라운드 동기화
  - [ ] 충돌 해결 (마지막 수정 우선)
- [ ] WorkManager로 주기적 동기화
- [ ] 네트워크 상태 감지
- [ ] 3대 기기 테스트
- [ ] 테스트 작성
- [ ] main 머지 후 v1.3.0-alpha 릴리즈

**참고:**
- 현재 로컬 전용 (각 기기 독립적)
- 사용자는 스마트폰 3대 사용 중
- 작업 단위별 실시간 동기화 필요

---

### 4. 🗂️ 필터링 & 정렬 (feature/filter-sort)
**우선순위: 중간**
- [ ] 카테고리별 필터 UI
- [ ] 우선순위별 필터 UI
- [ ] 날짜별 필터 (오늘/이번주/이번달)
- [ ] 정렬 옵션 (날짜/우선순위/제목)
- [ ] 필터 칩(Chip) UI 구현
- [ ] ViewModel 로직 추가
- [ ] 테스트 작성
- [ ] main 머지 후 v1.4.0 릴리즈

---

### 5. 📊 통계/대시보드 (feature/statistics)
**우선순위: 낮음**
- [ ] 통계 화면 추가
- [ ] 완료율 표시
- [ ] 카테고리별 분포 차트
- [ ] 우선순위별 분포
- [ ] 주간/월간 통계
- [ ] Chart 라이브러리 선택 (MPAndroidChart 등)
- [ ] 테스트 작성
- [ ] main 머지 후 v1.5.0 릴리즈

---

### 6. 🎨 테마/설정 (feature/theme-settings)
**우선순위: 낮음**
- [ ] 설정 화면 추가
- [ ] 다크/라이트 모드 토글
- [ ] 테마 컬러 선택
- [ ] DataStore로 설정 저장
- [ ] 시스템 테마 따르기 옵션
- [ ] 테스트 작성
- [ ] main 머지 후 v1.6.0 릴리즈

---

## 📋 세션 재시작 시 체크리스트

1. **프로젝트 상태 확인**
   ```bash
   git branch -a          # 브랜치 목록 확인
   git log --oneline -5   # 최근 커밋 확인
   git status             # 현재 상태
   ```

2. **다음 작업 선택**
   - 이 TODO.md 파일 읽기
   - 우선순위에 따라 feature 브랜치 선택
   - 해당 브랜치로 체크아웃

3. **개발 시작**
   ```bash
   git checkout feature/[기능명]
   ```

4. **TDD 규칙 준수**
   - 테스트 먼저 작성
   - 구현은 나중에
   - CLAUDE.md 규칙 확인

---

## 🎯 권장 작업 순서

1. **feature/datetime-picker** (v1.1.0)
   - 가장 기본적이고 필수적인 기능
   - dueDateTime 필드 활용 가능

2. **feature/notification** (v1.2.0)
   - 날짜/시간 선택이 먼저 필요
   - 리마인더 앱의 핵심 기능

3. **feature/filter-sort** (v1.4.0)
   - 사용성 개선
   - 데이터 많아지면 필수

4. **feature/firebase-sync** (v1.3.0-alpha)
   - 복잡한 기능, 테스트 단계
   - 기본 기능 안정화 후 진행

5. **feature/statistics** (v1.5.0)
   - 부가 기능

6. **feature/theme-settings** (v1.6.0)
   - UI 개선

---

## 📝 참고 사항

- **커밋 메시지**: 한글로 작성 (type(scope): 제목)
- **테스트**: 항상 먼저 작성 (TDD)
- **민감 정보**: 절대 커밋 금지
- **아키텍처**: MVVM 엄수

---

## 🔗 관련 문서

- [CLAUDE.md](./CLAUDE.md) - 프로젝트 개발 가이드
- [CHANGELOG.md](./CHANGELOG.md) - 변경 이력
- [README.md](./README.md) - 프로젝트 소개

---

**마지막 업데이트:** 2025-10-07
**현재 버전:** v1.0.0
**다음 목표:** v1.1.0 (날짜/시간 선택 UI)
