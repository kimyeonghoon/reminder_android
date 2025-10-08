# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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

[1.7.0]: https://github.com/yourusername/reminder/releases/tag/v1.7.0
[1.0.0]: https://github.com/yourusername/reminder/releases/tag/v1.0.0
