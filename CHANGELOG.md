# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.8.0] - 2025-10-09

### Added
- **접근성 개선**: TalkBack 스크린 리더 완벽 지원
  - 모든 UI 요소에 한글 contentDescription 추가
  - 우선순위, 완료 상태, 버튼 등 명확한 음성 안내
- **글씨 크기 조절**: 4단계 글씨 크기 설정
  - 작게 (0.85배), 보통 (1.0배), 크게 (1.15배), 아주 크게 (1.3배)
  - 전체 앱 Typography 동적 스케일링
- **간편 모드**: 70세+ 사용자를 위한 단순화 인터페이스
  - 복잡한 기능 숨김 (필터, 정렬, 통계, 검색)
  - 더 큰 버튼 (FAB 72dp, 아이콘 36dp)
  - 카테고리, 반복 설정 등 고급 기능 자동 숨김
- **도움말 화면**: 앱 내 종합 사용 가이드
  - 주요 기능 설명 (8개 항목)
  - 설정 기능 안내 (3개 항목)
  - 자주 묻는 질문 FAQ (5개 항목, 펼치기/접기 가능)
- **음성 입력**: 할 일 제목 음성으로 입력
  - Android Speech Recognition API 통합
  - 한국어 음성 인식 (ko-KR)
  - 큰 마이크 버튼 (56dp) - 노년층 사용자 배려
  - RECORD_AUDIO 권한 동적 요청

### Changed
- **설정 화면**: 간편 모드 시 동적 컬러 옵션 자동 숨김
- **홈 화면**: 간편 모드 시 FAB 크기 1.6배 확대
- **할 일 추가 화면**: 간편 모드 시 카테고리/반복 섹션 숨김

### Features
- **동적 Typography**: 사용자 설정에 따른 실시간 글씨 크기 변경
- **조건부 UI**: simpleMode 파라미터 기반 UI 요소 동적 표시/숨김
- **FAQ 컴포넌트**: 클릭으로 펼치기/접기 가능한 대화형 FAQ 카드
- **음성 권한 처리**: ActivityResultContracts로 권한 요청 및 처리

### Technical Details
- TDD 방식 구현 (AccessibilityTest.kt 작성)
- DataStore에 fontSize, simpleMode 필드 추가
- getTypography() 함수로 동적 Typography 생성
- Speech Recognition launcher 패턴 구현
- @OptIn(ExperimentalMaterial3Api::class) for Card onClick
- 모든 Screen에 simpleMode 파라미터 전파

### Accessibility
- 체크박스: "완료 여부 체크박스"
- 우선순위: "우선순위: 높음/중간/낮음"
- 삭제 버튼: "할 일 삭제"
- 통계 차트: 상세한 음성 설명 (예: "완료율 75 퍼센트")

## [1.7.0] - 2025-10-08

### Added
- **홈 화면 위젯**: 홈 화면에서 할 일 목록 바로 확인
- **위젯 완료 체크**: 위젯에서 직접 할 일 완료 처리 가능
- **다크 모드 위젯**: 시스템 다크 모드 자동 대응
- **실시간 자동 업데이트**: 할 일 추가/수정/삭제 시 위젯 자동 갱신

### Changed
- **앱 이름**: "Reminder" → "할 일 관리" (70대 사용자 친화)
- **위젯 텍스트**: 모든 영어 표시를 한글로 변경

### Features
- **위젯 정렬**: 마감일 가까운 순서로 자동 정렬 (최대 10개 표시)
- **우선순위 표시**: 좌측 색상 바로 우선순위 구분
- **새로고침 버튼**: 수동 위젯 업데이트 지원
- **앱 바로가기**: 위젯 항목 클릭 시 앱 열기

### Technical Details
- TDD 방식 구현 (테스트 우선 작성)
- RemoteViewsService/Factory 패턴
- AppWidgetProvider 커스텀 구현
- Repository 변경 자동 감지 및 위젯 업데이트

## [1.0.0] - 2025-10-07

### Added
- 기본 MVVM 아키텍처 구현
- Room Database를 사용한 로컬 데이터 저장
- 리마인더 CRUD 기능 (생성, 조회, 수정, 삭제)
- 리마인더 완료/미완료 토글 기능
- 우선순위 시스템 (낮음, 중간, 높음)
- 카테고리 분류 기능
- 검색 기능 (제목, 설명, 카테고리 기반)
- Material 3 디자인 시스템 적용
- 동적 컬러 지원 (Android 12+)
- ViewModel 단위 테스트 (100% 커버리지)
- Repository 단위 테스트 (100% 커버리지)
- DAO 통합 테스트 (주요 쿼리 100% 커버리지)

### Features
- **홈 화면**: 활성 리마인더 목록 표시
- **추가/편집 화면**: 리마인더 생성 및 수정
- **검색**: 실시간 검색 기능
- **정렬**: 마감일 및 우선순위 기반 자동 정렬
- **우선순위 표시**: 컬러 인디케이터로 시각적 구분

### Technical Details
- Kotlin 1.9.20
- Jetpack Compose
- Room Database 2.6.1
- MVVM Architecture
- Coroutines & Flow
- Navigation Compose
- Min SDK 26 (Android 8.0)
- Target SDK 34 (Android 14)

[1.8.0]: https://github.com/yourusername/reminder/releases/tag/v1.8.0
[1.7.0]: https://github.com/yourusername/reminder/releases/tag/v1.7.0
[1.0.0]: https://github.com/yourusername/reminder/releases/tag/v1.0.0
