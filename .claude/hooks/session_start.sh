#!/bin/bash

# SessionStart Hook: CLAUDE.md 자동 로드
# 세션이 시작될 때 자동으로 프로젝트 규칙을 읽어옵니다.

CLAUDE_MD="$CLAUDE_PROJECT_DIR/CLAUDE.md"

if [ -f "$CLAUDE_MD" ]; then
    CONTENT=$(cat "$CLAUDE_MD")

    # JSON 형식으로 출력
    cat << EOF
{
  "hookSpecificOutput": {
    "hookEventName": "SessionStart",
    "additionalContext": "📋 프로젝트 규칙 자동 로드\n\n아래는 이 프로젝트의 개발 규칙입니다. 반드시 숙지하고 따라주세요:\n\n$CONTENT"
  }
}
EOF
else
    echo '{"hookSpecificOutput": {"hookEventName": "SessionStart"}}'
fi
