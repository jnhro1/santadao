#!/usr/bin/env bash
# PreToolUse(Bash): 위험한 명령어를 차단합니다. exit 2 → 실행 차단 + stderr를 Claude에게 전달.
cmd=$(jq -r '.tool_input.command // empty')
[ -z "$cmd" ] && exit 0

block() {
    echo "🚫 차단된 명령어: $1" >&2
    echo "필요하다면 사용자가 직접 실행하도록 안내하세요." >&2
    exit 2
}

# 루트/홈/현재 디렉터리 전체 삭제
if echo "$cmd" | grep -Eq 'rm[[:space:]]+(-[a-zA-Z]*[rR][a-zA-Z]*[fF]?|-[a-zA-Z]*[fF][a-zA-Z]*[rR])[a-zA-Z]*[[:space:]]+(/|~|\$HOME|\.|\*)([[:space:]]|/?$)'; then
    block "광범위한 rm -rf"
fi

# force push (보호 브랜치 여부와 무관하게 차단, --force-with-lease 포함)
if echo "$cmd" | grep -Eq 'git[[:space:]]+push.*([[:space:]]-f([[:space:]]|$)|--force)'; then
    block "git push --force"
fi

# main/develop 직접 push
if echo "$cmd" | grep -Eq 'git[[:space:]]+push[[:space:]]+[^|;&]*[[:space:]:](main|develop)([[:space:]]|$)'; then
    block "main/develop 브랜치 직접 push"
fi

# 작업 내용을 날리는 git 명령
if echo "$cmd" | grep -Eq 'git[[:space:]]+(reset[[:space:]]+--hard|clean[[:space:]]+-[a-zA-Z]*f|checkout[[:space:]]+--[[:space:]]+\.|restore[[:space:]]+\.)'; then
    block "커밋되지 않은 변경사항을 삭제하는 git 명령"
fi

# DB 파괴적 SQL
if echo "$cmd" | grep -Eiq '(drop[[:space:]]+(database|schema|table)|truncate[[:space:]]+table)'; then
    block "파괴적인 SQL (DROP/TRUNCATE)"
fi

exit 0
