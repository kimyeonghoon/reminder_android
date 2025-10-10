# 다음 작업 계획

> 마지막 업데이트: 2025-10-10 (v1.36.0 완료)
> **📌 다음 세션 시작 시 CLAUDE.md를 먼저 읽으세요!**

---

## 📊 현재 프로젝트 현황

- **최신 버전**: v1.36.0 (versionCode 39)
- **DB 버전**: v15
- **총 릴리즈**: 36개 버전

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

---

## 🔥 다음 5개 버전 계획 (v1.37.0 ~ v1.41.0)

### v1.37.0: AI 스마트 추천 🤖

**목표**: 머신러닝 기반 지능형 리마인더 관리

**주요 기능**:

#### 1. 우선순위 자동 제안
**파일**: `app/src/main/java/com/reminder/ml/PriorityPredictor.kt` (신규)
- ML Kit Text Classification 활용
- 제목/설명 기반 우선순위 예측
- 과거 패턴 학습 (높은 우선순위로 설정한 키워드)
- TDD: `PriorityPredictorTest.kt` (5개 테스트)

#### 2. 마감일 스마트 제안
**파일**: `app/src/main/java/com/reminder/ml/DueDateSuggester.kt` (신규)
- 유사한 리마인더의 평균 완료 시간 분석
- 카테고리별 소요 시간 패턴 학습
- "이 작업은 보통 3일이 걸립니다" 메시지 표시

#### 3. 카테고리 자동 분류 강화
**파일**: `app/src/main/java/com/reminder/ml/CategoryClassifier.kt` (신규)
- 기존 v1.25.0 기능 강화 (통계 기반 → ML 기반)
- TensorFlow Lite 모델 온디바이스 추론
- 10개 이상 카테고리 학습 데이터 필요

#### 4. 스마트 알림 시간 제안
**파일**: `app/src/main/java/com/reminder/ml/NotificationTimeSuggester.kt` (신규)
- 사용자의 완료 시간대 패턴 분석
- "보통 오전 10시에 이 작업을 완료하세요" 제안
- 요일별, 카테고리별 최적 시간 학습

#### 5. UI 통합
**파일**: `app/src/main/java/com/reminder/ui/components/SmartSuggestionChip.kt` (신규)
- AI 제안 칩 표시 (우선순위, 마감일, 카테고리)
- "AI 제안 사용" 버튼
- 학습 데이터 피드백 수집

**기술 스택**:
- ML Kit (Text Classification, Entity Extraction)
- TensorFlow Lite (온디바이스 추론)
- Room Database (학습 데이터 저장)
- Kotlin Coroutines (비동기 추론)

**예상 시간**: 4-5시간
**DB 변경**: v15 → v16 (ml_training_data 테이블 추가)
**Files**: 8개 신규 (PriorityPredictor, DueDateSuggester, CategoryClassifier, NotificationTimeSuggester, SmartSuggestionChip + Tests)

---

### v1.38.0: 오프라인 모드 강화 🔄

**목표**: 네트워크 없이도 완벽하게 동작하는 오프라인 퍼스트 앱

**주요 기능**:

#### 1. 오프라인 작업 큐 시스템
**파일**: `app/src/main/java/com/reminder/sync/OfflineQueue.kt` (신규)
- Room 기반 작업 큐 (PendingAction 엔티티)
- CRUD 작업을 큐에 저장
- 네트워크 복구 시 자동 동기화
- TDD: `OfflineQueueTest.kt` (10개 테스트)

#### 2. 충돌 해결 전략 개선
**파일**: `app/src/main/java/com/reminder/sync/ConflictResolver.kt` (신규)
- Last Write Wins (기본)
- Manual Resolution (사용자 선택)
- Field-level Merge (필드별 병합)
- 충돌 기록 저장 (ConflictLog 엔티티)

#### 3. 동기화 상태 UI
**파일**: `app/src/main/java/com/reminder/ui/components/SyncStatusBanner.kt` (신규)
- 오프라인 상태 배너
- 동기화 진행률 표시
- 충돌 발생 시 알림
- "지금 동기화" 버튼

#### 4. 네트워크 상태 모니터링
**파일**: `app/src/main/java/com/reminder/network/NetworkMonitor.kt` (신규)
- ConnectivityManager 기반 모니터링
- Wi-Fi/모바일 데이터 구분
- 자동 재연결 로직
- WorkManager 통합 (네트워크 복구 시 동기화)

#### 5. 오프라인 우선 아키텍처
**파일**: `app/src/main/java/com/reminder/data/repository/OfflineFirstRepository.kt` (신규)
- 로컬 DB 먼저 쓰기
- 백그라운드 Firebase 동기화
- 낙관적 업데이트 (Optimistic UI)

**기술 스택**:
- Room Database (작업 큐, 충돌 로그)
- WorkManager (네트워크 제약 조건)
- ConnectivityManager
- StateFlow (동기화 상태 관리)

**예상 시간**: 3-4시간
**DB 변경**: v16 → v17 (pending_actions, conflict_logs 테이블)
**Files**: 7개 신규 + 3개 수정

---

### v1.39.0: 첨부파일 시스템 고도화 📎

**목표**: 다양한 파일 형식 지원 및 클라우드 스토리지 통합

**주요 기능**:

#### 1. 파일 첨부 확장
**파일**: `app/src/main/java/com/reminder/attachment/FileAttachmentManager.kt` (신규)
- PDF, DOC, XLS, TXT 파일 첨부
- 파일 크기 제한 (10MB)
- 로컬 저장 + Firebase Storage 백업
- ReminderAttachment 엔티티 (fileName, fileType, filePath, fileSize)

#### 2. 클라우드 스토리지 통합
**파일**: `app/src/main/java/com/reminder/cloud/CloudStorageProvider.kt` (신규)
- Google Drive API 연동
- Dropbox API 연동
- OneDrive API 연동 (선택)
- OAuth 2.0 인증
- 파일 picker 통합

#### 3. 첨부파일 미리보기
**파일**: `app/src/main/java/com/reminder/ui/components/AttachmentPreview.kt` (신규)
- 이미지 미리보기 (기존 기능 개선)
- PDF 뷰어 (PDFBox Android 또는 WebView)
- 문서 요약 정보 (파일명, 크기, 수정일)
- 다운로드/공유 버튼

#### 4. OCR 기능
**파일**: `app/src/main/java/com/reminder/ocr/TextRecognizer.kt` (신규)
- ML Kit Text Recognition
- 이미지에서 텍스트 추출
- 명함/영수증 스캔 → 리마인더 자동 생성
- TDD: `TextRecognizerTest.kt`

#### 5. 첨부파일 관리 화면
**파일**: `app/src/main/java/com/reminder/ui/screen/AttachmentsScreen.kt` (신규)
- 모든 첨부파일 목록
- 파일 타입별 필터 (이미지, PDF, 문서)
- 용량 통계 (총 사용량)
- 일괄 삭제/다운로드

**기술 스택**:
- Firebase Storage (파일 백업)
- Google Drive API, Dropbox API
- ML Kit Text Recognition
- AndroidPdfViewer 또는 WebView
- Coil (이미지 로딩)

**예상 시간**: 5-6시간
**DB 변경**: v17 → v18 (attachments 테이블 확장)
**Files**: 9개 신규 + 4개 수정

---

### v1.40.0: 캘린더 통합 📅

**목표**: Google Calendar 및 기기 캘린더와 양방향 동기화

**주요 기능**:

#### 1. Google Calendar 연동
**파일**: `app/src/main/java/com/reminder/calendar/GoogleCalendarSync.kt` (신규)
- Google Calendar API v3
- OAuth 2.0 인증
- 리마인더 → 캘린더 이벤트 자동 생성
- 캘린더 이벤트 → 리마인더 가져오기
- 양방향 동기화

#### 2. 기기 캘린더 통합
**파일**: `app/src/main/java/com/reminder/calendar/DeviceCalendarProvider.kt` (신규)
- CalendarContract API 사용
- 읽기/쓰기 권한 요청
- 다중 캘린더 지원 (직장, 개인 등)

#### 3. 캘린더 뷰
**파일**: `app/src/main/java/com/reminder/ui/screen/CalendarViewScreen.kt` (신규)
- 월간 캘린더 뷰 (ComposeCalendar 라이브러리)
- 날짜별 리마인더 표시
- 드래그 앤 드롭으로 날짜 변경
- 색상 구분 (우선순위별, 카테고리별)

#### 4. CalDAV 지원 (선택)
**파일**: `app/src/main/java/com/reminder/calendar/CalDAVClient.kt` (신규)
- CalDAV 프로토콜 지원
- iCloud, Nextcloud 캘린더 연동
- vCalendar 포맷 변환

#### 5. 캘린더 설정
**파일**: `app/src/main/java/com/reminder/ui/screen/CalendarSettingsScreen.kt` (신규)
- 동기화 대상 캘린더 선택
- 동기화 방향 설정 (단방향/양방향)
- 자동 동기화 주기
- 캘린더 색상 매핑

**기술 스택**:
- Google Calendar API v3
- CalendarContract (Android)
- ComposeCalendar 라이브러리
- CalDAV4j (선택)
- OAuth 2.0

**예상 시간**: 4-5시간
**DB 변경**: v18 → v19 (calendar_sync_config 테이블)
**Files**: 8개 신규 + 2개 수정

---

### v1.41.0: 웨어러블 지원 ⌚

**목표**: Wear OS 앱으로 손목에서 리마인더 관리

**주요 기능**:

#### 1. Wear OS 앱 모듈
**파일**: `wear/src/main/java/com/reminder/wear/MainActivity.kt` (신규 모듈)
- 독립 실행형 Wear OS 앱
- Tile API (홈 화면 타일)
- Complication API (워치페이스 컴플리케이션)

#### 2. 리마인더 목록 (Wear)
**파일**: `wear/src/main/java/com/reminder/wear/ui/ReminderListScreen.kt` (신규)
- ScalingLazyColumn 사용
- 스와이프로 완료/삭제
- 우선순위 색상 표시
- 햅틱 피드백

#### 3. 음성 입력 (Wear)
**파일**: `wear/src/main/java/com/reminder/wear/voice/VoiceInputHandler.kt` (신규)
- Speech-to-Text
- "OK Google, 우유 사기 리마인더 추가" 지원
- 빠른 리마인더 생성

#### 4. 스마트워치 알림
**파일**: `wear/src/main/java/com/reminder/wear/notification/WearNotificationManager.kt` (신규)
- Wear OS 전용 알림
- 액션 버튼 (완료, 스누즈, 삭제)
- 진동 패턴 커스터마이징

#### 5. Data Layer 동기화
**파일**: `app/src/main/java/com/reminder/wear/WearDataSync.kt` (신규)
- Wearable Data Layer API
- 폰 ↔ 워치 실시간 동기화
- MessageClient (즉시 전송)
- DataClient (영구 저장)

**기술 스택**:
- Wear OS 4.0
- Compose for Wear OS
- Wearable Data Layer API
- Horologist (Wear OS 라이브러리)
- Speech Recognition API

**예상 시간**: 6-7시간
**DB 변경**: 없음 (Wear OS는 폰 DB 공유)
**Files**: 12개 신규 (wear 모듈)

---

## 📅 예상 일정

- **v1.37.0** (AI 스마트 추천): 다음 세션
- **v1.38.0** (오프라인 모드 강화): 다음 세션 + 1
- **v1.39.0** (첨부파일 고도화): 다음 세션 + 2
- **v1.40.0** (캘린더 통합): 다음 세션 + 3
- **v1.41.0** (웨어러블 지원): 다음 세션 + 4

---

## 🎯 우선순위

1. **v1.37.0** - AI 스마트 추천 (필수, 생산성 대폭 향상)
2. **v1.38.0** - 오프라인 모드 강화 (높음, 안정성 개선)
3. **v1.39.0** - 첨부파일 고도화 (중간, 실용성 증대)
4. **v1.40.0** - 캘린더 통합 (중간, 타 서비스 통합)
5. **v1.41.0** - 웨어러블 지원 (낮음, 편의성, 복잡도 높음)

---

## 🚀 빠른 시작 가이드

### 다음 세션 시작 시:

```
"다음 작업 진행해줘"
```

그러면 v1.37.0 AI 스마트 추천부터 자동으로 시작됩니다.

특정 버전 지정:
```
"v1.38.0 오프라인 모드 강화 구현해줘 (TDD로)"
```

---

## 📝 참고사항

- **TDD 필수**: 모든 새 기능은 테스트 먼저 작성
- **커밋 메시지**: `type(scope): 한글 제목` 형식
- **버전 업데이트**: `app/build.gradle.kts`에서 versionCode/versionName 수정
- **문서화**: CHANGELOG.md, CLAUDE.md 동기화
- **API 키 관리**: local.properties에 저장 (절대 커밋 금지)

---

## ⚠️ 주의사항

### v1.37.0 (AI 스마트 추천)
- ML Kit 무료 티어 제한 확인
- TensorFlow Lite 모델 크기 (APK 용량 증가)
- 최소 학습 데이터 요구량 (10개 이상)

### v1.38.0 (오프라인 모드)
- 동기화 충돌 시나리오 철저한 테스트 필요
- 배터리 소모 최소화 (WorkManager 제약 조건)

### v1.39.0 (첨부파일)
- Firebase Storage 비용 (무료 티어: 5GB, 1GB/day 다운로드)
- Google Drive/Dropbox API 할당량 제한
- 파일 크기 제한 명확히 설정

### v1.40.0 (캘린더 통합)
- Google Calendar API 할당량 (10,000 requests/day)
- CalendarContract 권한 요청 UX
- 양방향 동기화 시 무한 루프 방지

### v1.41.0 (웨어러블)
- Wear OS 기기 테스트 필수
- 배터리 소모 최적화 (워치)
- 네트워크 비연결 시나리오 처리

---

## 🔗 관련 파일

- **프로젝트 가이드**: `CLAUDE.md` ⭐ (먼저 읽기)
- **변경 이력**: `CHANGELOG.md`
- **빌드 설정**: `app/build.gradle.kts`
- **아키텍처**: `ARCHITECTURE.md`

---

**Happy Coding! 🚀**

_다음 버전에서는 AI, 오프라인, 첨부파일, 캘린더, 웨어러블 기능으로 앱을 더욱 강력하게 만들어봅시다!_
