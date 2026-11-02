---
name: test
description: Gradle 테스트를 실행하고 실패 시 원인을 분석합니다. "테스트 돌려줘", "/test" 요청 시 사용합니다.
argument-hint: "[테스트 클래스/패턴 (선택)]"
allowed-tools: Bash(./gradlew test:*), Bash(./gradlew test), Read, Grep, Glob
---

# 테스트 실행 및 분석

1. 테스트 실행
   - 인자가 없으면: `./gradlew test`
   - 인자가 있으면: `./gradlew test --tests "$ARGUMENTS"` (클래스명만 주어지면 패키지 포함 FQCN 또는 `*클래스명` 패턴으로 변환)
2. 모두 통과하면 실행한 테스트 수와 함께 간단히 보고합니다.
3. 실패한 테스트가 있으면:
   - `build/test-results/test/*.xml` 또는 콘솔 출력에서 실패한 테스트와 스택트레이스를 추출합니다.
   - 관련 소스(테스트 + 대상 클래스)를 읽고 근본 원인을 분석합니다.
   - JPA 관련 실패라면 특히 확인: `LazyInitializationException`(트랜잭션 밖 지연 로딩), 영속성 컨텍스트 캐시(flush/clear 누락), 제약조건 위반, `ddl-auto`/스키마 불일치, 테스트 간 데이터 오염.
   - 원인, 수정 방안(테스트 수정인지 프로덕션 코드 수정인지 명확히)을 제시합니다.
   - 사용자가 수정까지 요청한 경우에만 코드를 고치고 다시 테스트를 실행합니다.

$ARGUMENTS
