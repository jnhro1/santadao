# santadao Backend

로그인 없이 쓰는 크리스마스 마니또 랜덤 추첨 웹 앱. 요구사항과 미결정 사항은 `docs/project-overview.md`를 참고합니다.

Spring Boot 4.1.2 · Java 17 · Spring Data JPA (Hibernate 7) + QueryDSL (OpenFeign 7.x) · Spring Security · springdoc · Gradle
DB: MySQL — 프로필별 스키마 `santadao-local`(local, 기본) / `santadao-test`(test) / `santadao`(prod)
설정: `src/main/resources/application.yml`(공통 DB 설정) + `application-{local,test,prod}.yml`(DB URL, 로그). `./gradlew test`는 자동으로 test 프로필
설정 원칙: 민감하지 않은 환경별 값(스키마명, 로그)은 `application-{profile}.yml`에 직접 적습니다. 민감하거나 서버마다 다른 값(`DB_HOST`, `DB_PORT`, `DB_USERNAME`, `DB_PASSWORD`)은 yml에 값·기본값 없이 참조만 하고, 모든 환경에서 같은 변수명을 씁니다(환경 접미사 금지)
값 주입: 로컬은 프로젝트 루트의 `application-secret.yml`(gitignore, 예시 `.example`), 서버(staging/prod)는 그 환경의 값을 환경변수로 넣습니다. 새 환경은 `application-{env}.yml` + 해당 서버 환경변수만 추가합니다
스키마: Flyway(`src/main/resources/db/migration`)만 관리하고 `ddl-auto`는 모든 프로필 `validate`. 엔티티 변경 시 `V{n}__설명.sql`을 함께 추가하고, 적용된 파일은 수정하지 않습니다. FK는 만들지 않고, 모든 테이블·컬럼에 한글 COMMENT(DDL)와 `comment` 속성(엔티티)을 답니다. 새 환경 최초 세팅은 `docker/mysql/init.sql`
패키지 루트: `com.jnhro1.santadao`

## Commands

```bash
./gradlew build                                   # 컴파일 + 테스트
./gradlew test --tests "com.jnhro1.santadao.album.*"   # 특정 테스트
./gradlew bootRun                                 # local 프로필, Swagger: /swagger-ui.html
```
로컬 `gradle`이 아닌 `./gradlew`만 사용합니다.

## Package Structure

도메인 단위로 나누고 내부를 계층별로 구성합니다. 공통 코드는 `global/`(annotation, config, common, exception, response, util).

```
com.jnhro1.santadao/{domain}/{controller,service,repository,entity,dto/request,dto/response}
```

## Core Rules

- 생성자 주입만 사용 (`@RequiredArgsConstructor` + `private final`)
- Entity는 Controller 입출력에 노출하지 않음 → 항상 DTO(record)
- `@Transactional`은 Service에만: 클래스 `readOnly = true`, 쓰기 메서드만 `@Transactional`
- 연관관계는 `FetchType.LAZY`, 목록 조회 N+1은 fetch join / `@EntityGraph`로 해결
- Entity에 `@Setter`/`@Data` 금지 → 의도가 드러나는 도메인 메서드로 상태 변경
- 들여쓰기 스페이스 4칸, 설정값·상수 하드코딩 금지

세부 규칙은 `.claude/rules/`에 있으며 관련 파일 작업 시 자동 로드됩니다.

## Workflow

- 코드 변경 후 `./gradlew test` 통과를 확인합니다.
- 새 의존성 추가 전 사용자에게 먼저 알립니다.
- 커밋/푸시/PR은 사용자가 명시적으로 요청할 때만 합니다. 커밋·브랜치·PR 컨벤션은 각각 `/commit-message`, `/create-branch`, `/create-pr` 스킬에 있습니다.
