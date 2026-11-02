---
paths:
  - "src/test/**/*.java"
---

# Testing Rules

## 테스트 종류

| 대상 | 방식 | 비고 |
|------|------|------|
| Entity 도메인 로직 | 순수 단위 테스트 (JUnit 5 + AssertJ) | Spring 컨텍스트 없이 |
| Service | 단위 테스트 (`@ExtendWith(MockitoExtension.class)`) 또는 `@SpringBootTest` 통합 테스트 | |
| Repository | `@DataJpaTest` | 직접 작성한 `@Query`, Custom Repository 위주로 |
| Controller | `@WebMvcTest` + `MockMvc` (또는 `MockMvcTester`) | Service는 `@MockitoBean`, `@Import(SecurityConfig.class)`로 실제 보안 설정 적용 (모든 경로 허용·CSRF 꺼짐이라 `@WithMockUser`/`csrf()` 불필요) |

## 테스트 환경

- `./gradlew test`는 `test` 프로필로 실행되며 MySQL `santadao-test` 스키마를 사용합니다. MySQL이 떠 있어야 합니다.
- 테스트 컨텍스트가 뜰 때마다 `TestFlywayConfig`가 `santadao-test`를 비우고(clean) V1부터 다시 마이그레이션합니다. 이전 실행의 데이터가 남지 않으며, 마이그레이션이 빈 DB에서 처음부터 적용되는지도 함께 검증됩니다.
- `@DataJpaTest`도 임베디드 DB로 교체하지 않고 같은 스키마를 사용합니다 (`spring.test.database.replace: none`).
- test 프로필 설정은 `src/main/resources/application-test.yml`에 있습니다. DB 계정은 local과 같이 `application-secret.yml`에서 읽습니다.

## 작성 규칙

- 테스트 클래스: `{대상클래스}Test` (예: `AlbumServiceTest`).
- 메서드명은 한글 허용, `@DisplayName`으로 의도를 명확히: `앨범_제목이_비어있으면_예외가_발생한다()`.
- **given / when / then** 구조로 작성.
- 검증은 AssertJ(`assertThat`, `assertThatThrownBy`) 사용.
- 테스트 간 데이터 의존 금지. `@Transactional` 롤백 또는 `@AfterEach` 정리.
- JPA 테스트에서 영속성 컨텍스트 캐시로 인해 쿼리가 안 나가는 경우를 주의 → 필요 시 `em.flush(); em.clear();` 후 검증.
- N+1 수정 시 쿼리 수를 검증하는 테스트(Hibernate Statistics 등)를 고려.
- 외부 연동(메일, 스토리지 등)은 Mock 처리.

## 실행

```bash
./gradlew test
./gradlew test --tests "com.jnhro1.santadao.album.service.AlbumServiceTest"
```
