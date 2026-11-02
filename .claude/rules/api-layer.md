---
paths:
  - "src/main/java/**/controller/**/*.java"
  - "src/main/java/**/dto/**/*.java"
---

# API Layer Rules (Controller / DTO)

## 1. Controller

```java
@Tag(name = "Album", description = "앨범 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/albums")
public class AlbumController {

    private final AlbumService albumService;

    @Operation(summary = "앨범 생성", description = "새 앨범을 생성합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<AlbumResponse>> createAlbum(
            @Valid @RequestBody CreateAlbumRequest request) {
        AlbumResponse response = albumService.createAlbum(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(HttpStatus.CREATED, response));
    }

    @Operation(summary = "앨범 단건 조회")
    @ApiErrorCodeExample(ErrorCode.ALBUM_NOT_FOUND)
    @GetMapping("/{albumId}")
    public ApiResponse<AlbumResponse> getAlbum(@PathVariable Long albumId) {
        return ApiResponse.ok(albumService.getAlbum(albumId));
    }
}
```

**규칙:**
- Controller는 요청 바인딩/검증 → Service 호출 → 응답 변환만 담당. 비즈니스 로직 금지.
- Controller에서 Repository 직접 호출 금지.
- **Entity를 요청/응답으로 사용하지 않습니다.** 반드시 DTO.
- 요청 본문은 `@Valid`로 검증.
- 응답은 `global/response/ApiResponse<T>` 래퍼 사용 (`ApiResponse.ok(data)`, `ApiResponse.ok()`).
- HTTP 상태가 200이 아니면 본문도 맞춥니다: `ResponseEntity.status(CREATED).body(ApiResponse.of(HttpStatus.CREATED, data))`.
- URL은 복수형 명사 + kebab-case, 버전 prefix `/api/v1`.
- Swagger: `@Tag`로 그룹, `@Operation`으로 API 설명을 작성합니다.
- 응답할 수 있는 에러는 `@ApiErrorCodeExample({ErrorCode.X, ...})`로 문서화합니다.
  - `SwaggerConfig`가 HTTP 상태별로 예시를 묶어 줍니다.
  - `INTERNAL_ERROR`는 모든 API에 자동으로 추가되므로 적지 않습니다.
  - 서비스에 새 예외를 추가하면 해당 Controller 메서드의 애너테이션도 함께 갱신합니다.

## 2. DTO

```java
public record CreateAlbumRequest(
        @NotBlank @Size(max = 100) String title,
        @NotNull Long userId
) {
    public Album toEntity(User user) {
        return Album.builder()
                .title(title)
                .user(user)
                .build();
    }
}

public record AlbumResponse(Long id, String title, LocalDateTime createdAt) {

    public static AlbumResponse from(Album album) {
        return new AlbumResponse(album.getId(), album.getTitle(), album.getCreatedAt());
    }
}
```

**규칙:**
- DTO는 `record` 사용, `dto/request`, `dto/response`에 위치.
- 검증은 Bean Validation 애너테이션(`@NotBlank`, `@Size`, `@Email` ...)으로 DTO에 선언.
- Entity → DTO 변환은 `Response.from(entity)` 정적 팩토리, DTO → Entity는 `Request.toEntity(...)`.
- Response DTO 변환 시 지연 로딩 필드 접근은 트랜잭션 안(Service)에서 끝냅니다.

## 3. 응답 포맷

```java
// 형식: { "code": 200, "status": "OK", "message": "OK", "data": {...} }
return ApiResponse.ok(data);                          // 성공 (데이터 있음)
return ApiResponse.ok();                              // 성공 (데이터 없음, data: null)
return ApiResponse.of(HttpStatus.CREATED, data);      // 200 외 성공 상태
// 에러 응답은 GlobalExceptionHandler에서 일괄 처리: { "code": 404, "status": "NOT_FOUND", "message": "이벤트를 찾을 수 없어요.", "data": null }
```
