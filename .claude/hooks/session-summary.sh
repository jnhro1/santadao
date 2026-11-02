#!/usr/bin/env bash
# Stop: 응답 완료 후 Git 변경사항을 요약해 사용자에게 표시합니다.
cd "${CLAUDE_PROJECT_DIR:-.}" 2>/dev/null || exit 0
git rev-parse --is-inside-work-tree >/dev/null 2>&1 || exit 0

status=$(git status --porcelain 2>/dev/null)
[ -z "$status" ] && exit 0

total=$(echo "$status" | wc -l | tr -d ' ')
untracked=$(echo "$status" | grep -c '^??' || true)
deleted=$(echo "$status" | grep -Ec '^(D.|.D)' || true)
changed=$((total - untracked - deleted))
branch=$(git branch --show-current 2>/dev/null)

msg="📋 [$branch] 변경 파일 ${total}개 (변경 ${changed} / 신규 ${untracked} / 삭제 ${deleted})"
jq -n --arg m "$msg" '{systemMessage: $m}'
exit 0
