# Domain Templates (`Album` 예시)

## Entity

```java
package com.jnhro1.santadao.album.entity;

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

    @Builder
    private Album(String title, User user) {
        this.title = title;
        this.user = user;
    }

    public void update(String title) {
        this.title = title;
    }
}
```

## Repository

```java
public interface AlbumRepository extends JpaRepository<Album, Long> {

    @Query("select a from Album a join fetch a.user where a.id = :id")
    Optional<Album> findWithUserById(@Param("id") Long id);

    Page<Album> findAllByUserId(Long userId, Pageable pageable);
}
```
- fetch join 쿼리는 실제로 연관 엔티티를 응답에 쓸 때만 추가합니다.

## DTO

```java
public record CreateAlbumRequest(
        @NotBlank @Size(max = 100) String title
) {
    public Album toEntity(User user) {
        return Album.builder().title(title).user(user).build();
    }
}

public record UpdateAlbumRequest(
        @NotBlank @Size(max = 100) String title
) {
}

public record AlbumResponse(Long id, String title, LocalDateTime createdAt) {

    public static AlbumResponse from(Album album) {
        return new AlbumResponse(album.getId(), album.getTitle(), album.getCreatedAt());
    }
}
```

## Service

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlbumService {

    private final AlbumRepository albumRepository;

    public AlbumResponse getAlbum(Long albumId) {
        return AlbumResponse.from(findAlbumById(albumId));
    }

    public Page<AlbumResponse> getAlbums(Long userId, Pageable pageable) {
        return albumRepository.findAllByUserId(userId, pageable).map(AlbumResponse::from);
    }

    @Transactional
    public AlbumResponse createAlbum(User user, CreateAlbumRequest request) {
        Album album = albumRepository.save(request.toEntity(user));
        return AlbumResponse.from(album);
    }

    @Transactional
    public AlbumResponse updateAlbum(Long albumId, UpdateAlbumRequest request) {
        Album album = findAlbumById(albumId);
        album.update(request.title());   // dirty checking, save() 불필요
        return AlbumResponse.from(album);
    }

    @Transactional
    public void deleteAlbum(Long albumId) {
        albumRepository.delete(findAlbumById(albumId));
    }

    private Album findAlbumById(Long albumId) {
        return albumRepository.findById(albumId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ALBUM_NOT_FOUND));
    }
}
```

## Controller

```java
@Tag(name = "Album", description = "앨범 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/albums")
public class AlbumController {

    private final AlbumService albumService;

    @Operation(summary = "앨범 단건 조회")
    @ApiErrorCodeExample(ErrorCode.ALBUM_NOT_FOUND)
    @GetMapping("/{albumId}")
    public ApiResponse<AlbumResponse> getAlbum(@PathVariable Long albumId) {
        return ApiResponse.ok(albumService.getAlbum(albumId));
    }

    @Operation(summary = "앨범 수정")
    @PatchMapping("/{albumId}")
    public ApiResponse<AlbumResponse> updateAlbum(
            @PathVariable Long albumId,
            @Valid @RequestBody UpdateAlbumRequest request) {
        return ApiResponse.ok(albumService.updateAlbum(albumId, request));
    }

    @Operation(summary = "앨범 삭제")
    @DeleteMapping("/{albumId}")
    public ApiResponse<Void> deleteAlbum(@PathVariable Long albumId) {
        albumService.deleteAlbum(albumId);
        return ApiResponse.ok();
    }
}
```
- 인증이 도입되면 사용자 식별은 PathVariable/RequestParam이 아닌 인증 정보에서 가져오고, 소유자 검증을 Service에서 수행합니다.

## Tests

```java
class AlbumTest {

    @Test
    @DisplayName("앨범 제목을 수정한다")
    void update() {
        // given
        Album album = Album.builder().title("old").build();
        // when
        album.update("new");
        // then
        assertThat(album.getTitle()).isEqualTo("new");
    }
}

@ExtendWith(MockitoExtension.class)
class AlbumServiceTest {

    @Mock
    private AlbumRepository albumRepository;

    @InjectMocks
    private AlbumService albumService;

    @Test
    @DisplayName("존재하지 않는 앨범을 조회하면 NotFoundException이 발생한다")
    void getAlbum_notFound() {
        given(albumRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> albumService.getAlbum(1L))
                .isInstanceOf(NotFoundException.class);
    }
}
```
