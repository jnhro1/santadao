---
name: create-pr
description: 현재 브랜치의 변경사항을 로컬 코드 리뷰한 뒤 PR 템플릿 기반으로 본문을 작성하고 Pull Request를 생성합니다. "PR 만들어줘", "/create-pr" 요청 시 사용합니다.
argument-hint: "[base 브랜치 (기본: develop, 없으면 main)]"
allowed-tools: Bash(git status:*), Bash(git diff:*), Bash(git log:*), Bash(git branch:*), Bash(git fetch:*), Bash(git push:*), Bash(gh pr:*), Bash(./gradlew build), Bash(./gradlew test), Read, Write, Grep, Glob, Agent
---

# Pull Request 생성

## 1. 사전 확인

- `gh` CLI가 설치/인증되어 있는지 확인합니다 (`gh auth status`). 없으면 설치(`brew install gh`)와 `! gh auth login` 실행을 안내하고 중단합니다.
- 현재 브랜치가 `main`/`develop`이면 중단하고 `/create-branch`를 안내합니다.
- base 브랜치: `$ARGUMENTS` → 없으면 `develop` → 없으면 `main`.
- 커밋되지 않은 변경이 있으면 `/commit-message`로 먼저 커밋할지 묻습니다.
- 브랜치명이 컨벤션(`.claude/skills/create-branch/SKILL.md`)에 맞지 않으면 알려줍니다.

## 2. 변경 분석

- `git log <base>..HEAD --oneline`
- `git diff <base>...HEAD --stat`, `git diff <base>...HEAD`

## 3. 빌드 & 테스트

- `./gradlew build` 실행. 실패하면 원인을 보고하고 PR 생성을 중단합니다.

## 4. 로컬 코드 리뷰 (1단계 리뷰)

`code-reviewer` 에이전트에게 `<base>...HEAD` diff 리뷰를 맡깁니다. 리뷰 기준과 출력 형식은 에이전트 정의(`.claude/agents/code-reviewer.md`)를 따르므로 base 브랜치만 전달하면 됩니다.

결과를 `.claude/reviews/PR-{branch 이름의 / 를 - 로}-{YYYY-MM-DD}.md`에 저장합니다.

**Critical/High 이슈가 있으면** 사용자에게 보여주고 수정 후 진행할지, 그대로 PR을 만들지 확인합니다.

## 5. PR 본문 작성

`.github/pull_request_template.md`를 기반으로 작성합니다.
- 제목: 커밋 컨벤션(`.claude/skills/commit-message/SKILL.md`) 형식 (`feat: 앨범 생성 API 추가`)
- 본문: 작업 내용, 변경 사항, 테스트 내역, 리뷰 포인트 (로컬 리뷰에서 남은 Medium 이하 이슈 포함)

작성한 제목/본문을 사용자에게 보여주고 승인받습니다.

## 6. PR 생성

```bash
git push -u origin <branch>
gh pr create --base <base> --title "<title>" --body "<body>"
```

생성된 PR URL을 반환합니다. 이후 GitHub 측 자동 리뷰(2단계)가 설정되어 있다면 결과를 확인하도록 안내합니다.
