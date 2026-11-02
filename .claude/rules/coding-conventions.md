---
paths:
  - "src/**/*.java"
---

# Coding Conventions

## 1. Naming

### Classes
| Type | Pattern | Example |
|------|---------|---------|
| Controller | `{Domain}Controller` | `AlbumController`, `UserController` |
| Service | `{Domain}Service` | `AlbumService`, `UserService` |
| Repository | `{Domain}Repository` | `AlbumRepository` |
| Custom Repository | `{Domain}RepositoryCustom` / `{Domain}RepositoryImpl` | `AlbumRepositoryCustom` |
| Entity | `{Domain}` (단수형) + `@Table(name = "{domains}")` | `Album` → `albums` 테이블 |
| Request DTO | `{Action}{Domain}Request` | `CreateAlbumRequest`, `UpdateUserRequest` |
| Response DTO | `{Domain}Response`, `{Domain}{Detail}Response` | `AlbumResponse`, `AlbumDetailResponse` |
| Exception | `{Reason}Exception` | `NotFoundException`, `BadRequestException` |
| Config | `{Feature}Config` | `JpaConfig`, `WebConfig` |

> JPA Entity는 단수형 클래스명을 사용하고, 테이블명은 `@Table`로 복수형 snake_case를 명시합니다.

### Methods
| 계층 | 용도 | Pattern | Example |
|------|------|---------|---------|
| Service | 단건 조회 | `get{Domain}` (없으면 예외) / `find{Domain}` (Optional 반환) | `getAlbum(id)`, `findAlbum(id)` |
| Service | 목록 조회 | `get{Domain}s` | `getAlbums(userId, pageable)` |
| Service | 생성 / 수정 / 삭제 | `create{Domain}` / `update{Domain}` / `delete{Domain}` | `createAlbum()` |
| Repository | 조회 | Spring Data 쿼리 메서드 규칙 | `findByUserIdAndId()`, `existsByEmail()` |
| Entity | 상태 변경 | 의도가 드러나는 동사 | `changeTitle()`, `addPhoto()`, `delete()` |

### Variables
- **Boolean**: `is{Adjective}`, `has{Noun}`, `can{Verb}` → `isDeleted`, `hasPermission`
- **Collection**: 복수형 → `albums`, `userIds`
- **단일 객체**: 단수형 → `album`, `user`
- **상수**: `UPPER_SNAKE_CASE`

## 2. Code Style

```java
// Lombok + 생성자 주입
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlbumService {

    private final AlbumRepository albumRepository;
}

// Builder 패턴 (Entity는 생성자에 @Builder)
Album album = Album.builder()
        .user(user)
        .title(request.title())
        .build();

// Stream API
List<AlbumResponse> responses = albums.stream()
        .map(AlbumResponse::from)
        .toList();
```

- `Stream.toList()` 사용 (`collect(Collectors.toList())` 대신).
- DTO는 가능하면 `record`로 작성.
- `Optional`은 반환 타입으로만 사용하고, 필드/파라미터로 사용하지 않습니다.
- `System.out.println`, `e.printStackTrace()` 금지 → `@Slf4j` 로거 사용.
- 와일드카드 import(`*`) 금지, 미사용 import 제거.

## 3. Clean Code — 『읽기 좋은 코드가 좋은 코드다』

1. **이름에 정보 담기**: `list`, `temp` 대신 `userAlbums`, `retryCount`.
2. **오해할 수 없는 이름**: 경계가 모호하면 명시 → `getStories(startIndex, lastIndexExclusive)`.
3. **설명 변수 활용**
   ```java
   boolean hasPhoto = album.getPhotos().stream()
           .anyMatch(photo -> photo.getId().equals(photoId));
   if (hasPhoto) { ... }
   ```
4. **무관한 하위 문제 추출**: 검증 / 변환 / 저장 / 알림을 각각의 메서드로 분리.
5. **한 번에 하나씩**: 메서드는 하나의 작업, 클래스는 하나의 책임.
6. **Early return**으로 중첩을 줄입니다.
