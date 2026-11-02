---
paths:
  - "src/main/java/**/entity/**/*.java"
  - "src/main/java/**/repository/**/*.java"
  - "src/main/java/**/service/**/*.java"
  - "src/main/resources/application*.yml"
  - "src/main/resources/application*.properties"
  - "src/main/resources/db/migration/**/*.sql"
---

# JPA Rules

## 1. Entity 설계

```java
@Entity
@Table(name = "albums", comment = "앨범")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Album extends BaseEntity {

    @Column(nullable = false, length = 100, comment = "앨범 제목")
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT),
            comment = "작성자 ID (users.id)")
    private User user;

    @OneToMany(mappedBy = "album", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Photo> photos = new ArrayList<>();

    @Builder
    private Album(String title, User user) {
        this.title = title;
        this.user = user;
    }

    public void changeTitle(String title) {
        this.title = title;
    }

    public void addPhoto(Photo photo) {
        photos.add(photo);
        photo.assignAlbum(this);
    }
}
```

**규칙:**
- 기본 생성자는 `@NoArgsConstructor(access = AccessLevel.PROTECTED)`.
- `@Setter`, `@Data`, `@AllArgsConstructor`, `@ToString`(연관관계 포함) 사용 금지.
- `@Builder`는 클래스가 아닌 **생성자**에 붙여 필요한 필드만 받습니다.
- 상태 변경은 도메인 메서드(`changeTitle`, `delete`)로 — 변경은 Dirty Checking으로 반영되므로 `save()` 재호출 불필요.
- 모든 Entity는 `global/common/BaseEntity`를 상속합니다. `id`(IDENTITY), `createdAt`, `updatedAt`을 공통으로 가지므로 Entity에 `@Id`를 따로 선언하지 않습니다(`@MappedSuperclass` + `@EnableJpaAuditing`).
- **모든 테이블과 컬럼에 한글 설명을 답니다.** JPA 3.2 표준 속성을 사용합니다: `@Table(comment = "...")`, `@Column(comment = "...")`, `@JoinColumn(comment = "...")`.
  - Hibernate의 `@org.hibernate.annotations.Comment`는 deprecated이므로 쓰지 않습니다.
  - 마이그레이션 SQL의 `COMMENT`와 같은 문구로 맞춥니다. 실제 DB 주석은 Flyway가 만들고, 엔티티 쪽은 코드를 읽는 사람을 위한 문서 역할입니다.
  - 조인 컬럼은 참조 대상을 함께 적습니다: `"이벤트 ID (events.id)"`. FK가 없으므로 관계를 알려주는 유일한 단서입니다.
- Enum 컬럼은 반드시 `@Enumerated(EnumType.STRING)`. 주석에 가능한 값을 적습니다: `"상태 (READY, DONE)"`.
- `equals`/`hashCode`를 재정의해야 한다면 id 기반으로, 프록시를 고려해 작성합니다.

## 2. 연관관계

### FK 제약조건은 만들지 않는다
- DB에 **FK 제약조건을 생성하지 않습니다.** JPA 연관관계(`@ManyToOne`, fetch join 등)는 그대로 쓰되, 모든 `@JoinColumn`에 `foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)`를 지정합니다.
  - `@OneToOne`, `@ManyToMany`의 중간 테이블(`@JoinTable`)도 마찬가지입니다(`foreignKey`, `inverseForeignKey`).
  - 지정을 빠뜨리면 Hibernate가 FK를 만듭니다. `SchemaConventionTest`가 스키마에 FK가 하나라도 있으면 실패합니다.
- 참조 무결성은 애플리케이션에서 보장합니다.
  - 참조 대상이 있는지 Service에서 조회로 확인한 뒤 연결합니다(없으면 `NotFoundException`).
  - 부모를 삭제할 때 자식 처리(함께 삭제, 남겨 두기)를 Service나 `cascade`/`orphanRemoval`로 명시적으로 처리합니다. DB가 막아 주지 않으므로 고아 데이터가 생기지 않게 주의합니다.
- FK가 없으면 InnoDB가 조인 컬럼에 인덱스를 자동으로 만들지 않습니다. 조회·조인 조건으로 쓰는 조인 컬럼에는 `@Table(indexes = @Index(...))`로 인덱스를 직접 만듭니다. 그 컬럼으로 시작하는 unique 제약이 있으면 생략할 수 있습니다.

### 연관관계 매핑
- **모든 `@ManyToOne`, `@OneToOne`은 `fetch = FetchType.LAZY` 명시** (기본값이 EAGER).
- 기본은 **단방향 `@ManyToOne`**. 양방향은 실제로 필요할 때만 추가하고, 연관관계 편의 메서드를 둡니다.
- `@ManyToMany` 금지 → 중간 엔티티로 풀어서 매핑.
- `cascade` / `orphanRemoval`은 생명주기가 완전히 종속된 경우(예: Album → Photo)에만.
- 컬렉션 필드는 선언 시 초기화(`= new ArrayList<>()`)하고 외부에 setter로 교체하지 않습니다.

## 3. 트랜잭션

- 트랜잭션 경계는 **Service 계층**. Controller/Repository에 `@Transactional`을 두지 않습니다.
- 클래스 레벨 `@Transactional(readOnly = true)`, 쓰기 메서드에만 `@Transactional`.
- `@Transactional`은 `org.springframework.transaction.annotation.Transactional` 사용.
- 같은 클래스 내부 호출(self-invocation)은 프록시를 거치지 않아 트랜잭션이 적용되지 않음에 주의.
- 트랜잭션 안에서 외부 API 호출, 파일 업로드 등 느린 I/O를 하지 않습니다.
- `spring.jpa.open-in-view=false`로 설정하고, 지연 로딩은 Service 안에서 끝냅니다.

## 4. 조회 & 성능

- **N+1 방지**: 목록 조회에서 연관 엔티티를 쓰면 `fetch join` 또는 `@EntityGraph` 사용.
  ```java
  @Query("select a from Album a join fetch a.user where a.user.id = :userId")
  List<Album> findAllWithUserByUserId(@Param("userId") Long userId);
  ```
- 컬렉션 fetch join + 페이징 금지(메모리 페이징 발생) → `default_batch_fetch_size`(예: 100) 설정 후 지연 로딩으로 해결.
- 목록 API는 `Pageable` / `Slice`로 페이징합니다. 전체 조회(`findAll()`) 남용 금지.
- 조회 전용 화면은 DTO Projection(`select new ...` 또는 인터페이스 프로젝션) 고려.
- 대량 수정/삭제는 `@Modifying(clearAutomatically = true)` 벌크 쿼리 사용, 이후 영속성 컨텍스트 상태 주의.
- 존재 여부만 필요하면 `existsBy...` 사용 (`findBy...().isPresent()` 대신).
- 단건 조회 실패는 `orElseThrow(() -> new NotFoundException(ErrorCode.ALBUM_NOT_FOUND))`.

## 5. 쿼리 작성

- 간단한 조건 → 쿼리 메서드 (`findByUserIdAndStatus`).
- 메서드명이 길어지거나 join이 필요 → `@Query` (JPQL, 파라미터는 `:name` 바인딩).
- 동적 조건/복잡한 쿼리 → `{Domain}RepositoryCustom` + `{Domain}RepositoryImpl`에서 QueryDSL(`JPAQueryFactory` 주입) 사용.
  - import는 `com.querydsl.*` (OpenFeign fork, 패키지명 동일). Q클래스는 `build/generated/querydsl`에 생성되며 커밋하지 않습니다.
  - 동적 조건은 `BooleanExpression`을 반환하는 private 메서드로 분리하고 null이면 조건에서 제외되게 작성합니다.
- 문자열 연결로 JPQL/Native Query를 만들지 않습니다(인젝션 위험).
- Native Query는 최후의 수단으로만 사용하고 이유를 주석으로 남깁니다.

## 6. 동시성

- 재고/카운트 등 동시 수정이 일어나는 엔티티는 `@Version`(낙관적 락) 또는 `@Lock(PESSIMISTIC_WRITE)` 검토.
- "조회 후 없으면 생성" 패턴은 DB unique 제약조건으로 최종 보장합니다.

## 7. 스키마 관리 (Flyway)

- **스키마는 Flyway만 만들고 바꿉니다.** 모든 프로필이 `ddl-auto: validate`이고, Hibernate는 엔티티와 테이블이 맞는지 검사만 합니다.
- 마이그레이션 파일은 `src/main/resources/db/migration/V{번호}__{설명}.sql`입니다. 번호는 1씩 올리고, 설명은 snake_case로 씁니다(예: `V2__add_event_deadline.sql`).
- **엔티티를 바꾸면 같은 커밋에 마이그레이션 파일을 추가합니다.** 빠뜨리면 `./gradlew test`의 schema validation에서 실패합니다.
- **이미 적용된 마이그레이션 파일은 수정하거나 삭제하지 않습니다.** checksum이 달라져 모든 환경에서 앱이 뜨지 않습니다. 고칠 내용은 새 버전 파일로 추가합니다.
- 작성 규칙
  - FK 제약조건을 넣지 않습니다(2절). 조회 조건으로 쓰는 조인 컬럼에는 인덱스를 만듭니다.
  - **모든 테이블(`COMMENT = '...'`)과 컬럼(`COMMENT '...'`)에 한글 설명을 답니다.** 엔티티의 `comment` 속성과 같은 문구로 씁니다. 컬럼을 추가·변경하는 마이그레이션에도 COMMENT를 포함합니다.
  - 제약조건과 인덱스 이름을 명시합니다: `uk_{table}_{columns}`, `idx_{table}_{columns}`.
  - 대소문자를 구분해야 하는 코드·토큰 컬럼은 `COLLATE utf8mb4_bin`을 지정합니다. 기본 collation(`utf8mb4_0900_ai_ci`)은 대소문자를 구분하지 않습니다.
  - 데이터가 있는 테이블에 NOT NULL 컬럼을 추가할 때는 DEFAULT를 주거나, 추가 → 값 채우기 → 제약 추가 순서로 나눕니다.
- `SchemaConventionTest`가 FK 존재, 주석이 없는 테이블·컬럼을 검사합니다. `./gradlew test`에서 실패하면 마이그레이션을 고칩니다.
- 스키마(database)와 DB 계정 생성은 Flyway 범위가 아닙니다. 새 환경은 `docker/mysql/init.sql`로 스키마를 만든 뒤 앱을 실행합니다.

## 8. 설정

```yaml
spring:
  jpa:
    open-in-view: false
    hibernate:
      ddl-auto: validate      # 모든 프로필. 스키마는 Flyway가 관리
    properties:
      hibernate:
        default_batch_fetch_size: 100
```
