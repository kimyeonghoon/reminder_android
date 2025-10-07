# Claude Code 설정

이 디렉토리에는 Claude Code 관련 설정과 훅이 포함되어 있습니다.

## SessionStart Hook

### 목적
세션이 시작될 때 자동으로 `CLAUDE.md` 파일을 읽어서 프로젝트 규칙을 로드합니다.

### 동작
- 새 세션 시작 시 자동 실행
- CLAUDE.md 내용을 컨텍스트에 추가
- TDD, 민감정보 처리, 커밋 규칙 등을 자동으로 숙지

### 파일
- `settings.json` - Hook 설정
- `hooks/session_start.sh` - SessionStart 훅 스크립트

### 수동으로 규칙 확인하기
Hook이 작동하지 않는 경우, 다음과 같이 수동으로 요청하세요:
```
CLAUDE.md를 읽고 프로젝트 규칙을 확인해줘.
```

### 보안
- Hook 스크립트는 프로젝트 디렉토리 내에만 접근
- CLAUDE.md 파일만 읽음
- 외부 네트워크 접근 없음
