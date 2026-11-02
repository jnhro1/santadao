---
name: commit-message
description: 변경사항을 분석해 프로젝트 커밋 컨벤션에 맞는 커밋 메시지를 제안하고, 사용자 확인 후 커밋합니다. "커밋 메시지 만들어줘", "커밋해줘", "/commit-message" 요청 시 사용합니다.
allowed-tools: Bash(git status:*), Bash(git diff:*), Bash(git log:*), Bash(git add:*), Bash(git commit:*), Bash(./gradlew build), Read
---

# 커밋 메시지 작성

## 커밋 컨벤션

```
<type>: <subject>

[optional body]

[optional footer]
```

- subject: 한글, 50자 이내, 마침표 없이 "~ 추가 / ~ 수정 / ~ 변경" 형태
- body: **무엇을, 왜** 바꿨는지 (어떻게는 코드로). 필요할 때만 작성

| Type | Description | 예시 |
|------|-------------|------|
| `feat` | 새로운 기능 추가 | 앨범 생성 API 추가 |
| `fix` | 기능 변경 | 앨범 정렬 방식 변경 |
| `bugfix` | 버그 수정 | 앨범 삭제 시 NullPointerException 수정 |
| `refactor` | 기능 변화 없이 코드 구조 개선 | 앨범 조회 로직 메서드 분리 |
| `test` | 테스트 코드 추가 및 변경 | AlbumService 단위 테스트 추가 |
| `style` | 네이밍, 포맷, import 정리 등 | import 정리 |
| `review` | PR 리뷰 내용 반영 | 리뷰 피드백 반영 |
| `chore` | 빌드 설정, 의존성, 설정 파일 등 | Spring Data JPA 의존성 추가 |
| `docs` | 문서 수정 | CLAUDE.md 규칙 추가 |

> `fix`는 버그가 아닌 **기능 변경**입니다. 버그 수정은 반드시 `bugfix`를 사용합니다.

## 절차

1. **변경사항 확인**
   - `git status`, `git diff --staged`, `git diff`
   - staged 변경이 없으면 포함할 파일을 사용자에게 확인합니다. (`.claude/settings.local.json`, 빌드 산출물, 비밀정보 파일은 제외)
2. **커밋 전 점검**
   - Java 코드가 바뀌었으면 `./gradlew build` 통과 확인
   - 디버그 코드(`System.out`), 미사용 import, 하드코딩된 값이 없는지 diff에서 확인
   - API가 바뀌었으면 Swagger 문서(`@Operation`, `@Tag`) 갱신 여부 확인
3. **분석**: 변경 내용으로 type을 결정합니다. 성격이 다른 변경이 섞여 있으면 커밋을 나누자고 제안합니다 (예: 의존성 추가 `chore` + 기능 `feat`).
4. **제안**: 메시지를 보여주고 수정할지 확인합니다. `git log --oneline -10`의 기존 스타일과도 맞춥니다.
5. **커밋**: 승인받으면 `git add <파일>` → `git commit` 후 `git log --oneline -1`로 결과를 보여줍니다.

## 주의

- 사용자 승인 없이 커밋하지 않습니다. push는 별도 요청이 있을 때만 합니다.
- `main`/`develop`에 직접 커밋하려 하면 먼저 `/create-branch`를 제안합니다.
- `--no-verify`, `--amend`는 사용자가 명시적으로 요청할 때만 사용합니다.

## 예시

```
feat: 앨범 생성 API 추가
bugfix: 앨범 삭제 시 사진이 남아있는 문제 수정
chore: Spring Data JPA 및 H2 의존성 추가
refactor: AlbumService 조회 로직 메서드 분리
```
