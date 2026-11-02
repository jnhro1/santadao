---
name: create-branch
description: 변경사항이나 작업 설명을 분석해 브랜치 컨벤션에 맞는 이름으로 브랜치를 생성하고 체크아웃합니다. "브랜치 만들어줘", "/create-branch" 요청 시 사용합니다.
argument-hint: "[작업 설명 또는 hotfix 버전 (선택)]"
allowed-tools: Bash(git status:*), Bash(git diff:*), Bash(git branch:*), Bash(git log:*), Bash(git fetch:*), Bash(git switch:*), Bash(git checkout:*), Bash(git stash:*)
---

# 브랜치 생성

## 브랜치 컨벤션

| Prefix | 용도 | Postfix 규칙 | 예시 |
|--------|------|-------------|------|
| `feature/` | 모든 일반 작업 (기능, 버그 수정, 리팩터링) | 작업 내용을 영어 kebab-case 2~4단어로 | `feature/album-create`, `feature/fix-album-delete` |
| `hotfix/` | 긴급 프로덕션 수정 | 버전명 | `hotfix/1.0.4`, `hotfix/1.0.4-1` |

- 기준 브랜치: `develop` (없으면 `main`). hotfix는 `main`에서 분기.
- `feat/`, `fix/` 등 축약 prefix 금지, 대문자/camelCase 금지.
- `hotfix/`는 반드시 버전명 (설명 금지, 예: `hotfix/login-bug` ✗). 같은 버전의 추가 핫픽스는 `-1`, `-2` 접미사.
- `main`/`develop`에 직접 커밋·force push 금지.

## 절차

1. **현재 상태 확인**: `git status`, `git branch -a`, `git diff --stat`
2. **종류 결정**: 긴급 프로덕션 수정이면 `hotfix/` (버전을 모르면 사용자에게 묻습니다), 그 외 전부 `feature/`.
3. **이름 생성**: `$ARGUMENTS` 또는 변경사항에서 핵심 도메인/작업을 뽑아 만듭니다. 이미 같은 이름이 있으면 다른 이름을 제안합니다.
4. **사용자 확인**: 제안한 이름과 기준 브랜치를 보여주고 승인받습니다.
5. **생성**
   - 원격이 있으면 `git fetch` 후 최신 기준 브랜치에서 분기합니다.
   - 작업 중인 변경사항은 그대로 가져갑니다: `git switch -c <branch> <base>` (충돌 시 stash 여부를 사용자에게 확인).
6. `git branch --show-current`로 결과를 보고합니다.

$ARGUMENTS
