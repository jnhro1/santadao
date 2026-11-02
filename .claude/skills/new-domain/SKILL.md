---
name: new-domain
description: 새 도메인(Entity, Repository, Service, Controller, DTO, 테스트)의 뼈대를 프로젝트 컨벤션대로 생성합니다. "앨범 도메인 만들어줘", "User 엔티티랑 CRUD 추가해줘", "/new-domain album" 요청 시 사용합니다.
argument-hint: "<도메인명> [필드 설명]"
---

# 새 도메인 생성

`$ARGUMENTS`로 받은 도메인을 `com.jnhro1.santadao.{domain}` 아래에 생성합니다.

## 1. 요구사항 확인

다음이 불분명하면 **생성 전에** 사용자에게 한 번에 묻습니다.
- 필드 목록과 타입, 필수 여부, 길이 제한
- 연관관계 (어느 엔티티와 N:1 / 1:N, 생명주기 종속 여부 → cascade 결정)
- 필요한 API (CRUD 전체 / 일부, 목록 페이징 여부)
- 상태(enum) 필드, 소프트 삭제 여부

## 2. 선행 조건 점검

- 공통 클래스는 `global/`에 이미 있으므로 새로 만들지 않고 재사용합니다.
  - `common/BaseEntity`(id, createdAt, updatedAt), `response/ApiResponse`
  - `exception/ErrorCode`, `BusinessException`, `NotFoundException`, `BadRequestException`, `UnauthorizedException`
- 새 에러는 `ErrorCode`에 `{DOMAIN}_NOT_FOUND(HttpStatus.NOT_FOUND, "...")` 형태로 추가합니다.

## 3. 파일 생성

[templates/domain.md](templates/domain.md)를 기준으로 아래 순서대로 생성합니다. `Album`/`album`을 도메인명으로 치환합니다.

| 순서 | 파일 | 위치 |
|------|------|------|
| 1 | Entity | `{domain}/entity/{Domain}.java` |
| 2 | Repository | `{domain}/repository/{Domain}Repository.java` |
| 3 | Request/Response DTO | `{domain}/dto/request/`, `{domain}/dto/response/` |
| 4 | Service | `{domain}/service/{Domain}Service.java` |
| 5 | Controller | `{domain}/controller/{Domain}Controller.java` |
| 6 | ErrorCode 항목 | `global/exception/ErrorCode.java`에 `{DOMAIN}_NOT_FOUND` 추가 |
| 7 | 마이그레이션 | `src/main/resources/db/migration/V{다음 번호}__create_{domains}.sql` (FK 없이, 모든 테이블·컬럼에 한글 COMMENT, `.claude/rules/jpa.md` 7절 규칙). 엔티티에도 같은 문구로 `comment` 속성을 답니다 |
| 8 | 테스트 | Entity 단위 테스트, Service 테스트, (커스텀 쿼리가 있으면) `@DataJpaTest` |

요청받지 않은 API/필드는 만들지 않습니다.

## 4. 검증

- `./gradlew build`로 컴파일 + 테스트를 확인합니다.
- 생성한 파일 목록과 API 엔드포인트 목록(메서드, URL)을 보고합니다.
