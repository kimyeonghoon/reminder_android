#!/usr/bin/env python3
"""
Script to convert Korean test function names to English function names with Korean comments
"""

import re
import os
from pathlib import Path

# Mapping of common Korean test patterns to English names
FUNCTION_NAME_MAPPINGS = {
    # Common patterns
    "초기 상태": "initialState",
    "초기값": "initialValue",
    "기본값": "defaultValue",

    # Actions
    "추가": "add",
    "삭제": "delete",
    "수정": "update",
    "생성": "create",
    "변경": "change",
    "반환": "return",
    "반영": "reflect",
    "포함": "include",
    "표시": "display",
    "검색": "search",
    "필터링": "filter",
    "정렬": "sort",
    "선택": "select",
    "완료": "complete",
    "시작": "start",
    "종료": "end",
    "실행": "execute",
    "호출": "call",
    "전달": "pass",
    "업데이트": "update",
    "토글": "toggle",
    "이동": "move",
    "복사": "copy",
    "저장": "save",
    "로드": "load",
    "가져오기": "fetch",
    "계산": "calculate",
    "확인": "verify",
    "검증": "validate",

    # States
    "활성": "active",
    "비활성": "inactive",
    "진행": "progress",
    "대기": "idle",
    "완료됨": "completed",
    "실패": "failed",
    "성공": "success",

    # Comparisons
    "같다": "equals",
    "다르다": "differs",
    "크다": "greater",
    "작다": "less",
    "많다": "more",
    "적다": "fewer",

    # Objects
    "리마인더": "reminder",
    "할일": "task",
    "목록": "list",
    "항목": "item",
    "화면": "screen",
    "버튼": "button",
    "제목": "title",
    "설명": "description",
    "카테고리": "category",
    "우선순위": "priority",
    "날짜": "date",
    "시간": "time",
    "세션": "session",
    "포커스": "focus",
    "타이머": "timer",
    "쿼드런트": "quadrant",
    "통계": "statistics",
    "트렌드": "trend",
    "패턴": "pattern",
    "분포": "distribution",
    "긴급도": "urgency",
    "키워드": "keyword",
}

def korean_to_english_function_name(korean_text):
    """
    Convert Korean test description to English camelCase function name
    This is a simple heuristic-based approach
    """
    # Remove common test patterns
    text = korean_text.replace("_", " ").replace("`", "")

    # Split into words and try to translate
    words = text.split()
    english_words = []

    for word in words:
        # Check if word matches any mapping
        found = False
        for korean, english in FUNCTION_NAME_MAPPINGS.items():
            if korean in word:
                english_words.append(english)
                found = True
                break

        if not found:
            # Keep as is if no mapping (might be English already)
            if word.isalnum():
                english_words.append(word.lower())

    # Create camelCase
    if len(english_words) == 0:
        return "testFunction"

    result = english_words[0].lower()
    for word in english_words[1:]:
        result += word.capitalize()

    return result

def process_test_file(file_path):
    """
    Process a single test file to convert Korean function names
    """
    print(f"Processing: {file_path}")

    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()

    # Find all Korean function names
    # Pattern 1: fun `한글 함수명`()
    pattern1 = r'(@Test\s+fun\s+`([^`]+)`\()'
    # Pattern 2: fun 한글_함수명()
    pattern2 = r'(@Test\s+fun\s+([ㄱ-ㅎㅏ-ㅣ가-힣_]+)\()'

    modifications = 0

    # Process pattern 1 (backtick functions)
    def replace_pattern1(match):
        nonlocal modifications
        korean_name = match.group(2)
        english_name = korean_to_english_function_name(korean_name)
        modifications += 1
        return f"/**\n     * {korean_name}\n     */\n    @Test\n    fun {english_name}("

    content = re.sub(pattern1, replace_pattern1, content, flags=re.MULTILINE)

    # Process pattern 2 (underscore functions)
    def replace_pattern2(match):
        nonlocal modifications
        korean_name = match.group(2)
        # Convert underscores to spaces for better description
        korean_desc = korean_name.replace("_", " ")
        english_name = korean_to_english_function_name(korean_name)
        modifications += 1
        return f"/**\n     * {korean_desc}\n     */\n    @Test\n    fun {english_name}("

    content = re.sub(pattern2, replace_pattern2, content, flags=re.MULTILINE)

    if modifications > 0:
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"  ✓ Modified {modifications} functions")
        return modifications
    else:
        print(f"  - No Korean function names found")
        return 0

def main():
    # Base paths
    base_paths = [
        "/home/ioniere/projects/reminder/app/src/test/java",
        "/home/ioniere/projects/reminder/app/src/androidTest/java"
    ]

    total_files = 0
    total_functions = 0

    for base_path in base_paths:
        if not os.path.exists(base_path):
            continue

        # Find all *Test.kt files
        for test_file in Path(base_path).rglob("*Test.kt"):
            total_files += 1
            count = process_test_file(str(test_file))
            total_functions += count

    print(f"\n=== Summary ===")
    print(f"Total files processed: {total_files}")
    print(f"Total functions converted: {total_functions}")

if __name__ == "__main__":
    main()
