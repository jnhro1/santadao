---
paths:
  - "src/main/java/**/*.java"
---

# Exception Handling

## 구조

```
global/exception/
├── ErrorCode.java               # enum: code(HttpStatus) + message
├── BusinessException.java       # RuntimeException, ErrorCode 보유
├── NotFoundException.java       # extends BusinessException
├── BadRequestException.java     # extends BusinessException
├── UnauthorizedException.java   # extends BusinessException
└── GlobalExceptionHandler.java  # @RestControllerAdvice
```

```java
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값을 다시 확인해 주세요."),
    ALBUM_NOT_FOUND(HttpStatus.NOT_FOUND, "앨범을 찾을 수 없어요.");

    private final HttpStatus code;   // ApiResponse의 code(숫자)/status(이름)로 그대로 쓰인다
    private final String message;    // 화면에 그대로 표시되므로 사용자용 한국어 문장으로 작성
}
```

## 사용 규칙

```java
// 리소스 없음
Album album = albumRepository.findById(albumId)
        .orElseThrow(() -> new NotFoundException(ErrorCode.ALBUM_NOT_FOUND));

// 잘못된 요청
if (!album.isOwnedBy(userId)) {
    throw new BadRequestException(ErrorCode.INVALID_INPUT);
}
```

- 비즈니스 예외는 `BusinessException` 계열 + `ErrorCode`로 던집니다. `RuntimeException`/`Exception`을 직접 던지지 않습니다.
- 예외를 잡아서 무시(빈 catch)하지 않습니다. 잡았다면 로그를 남기거나 의미 있는 예외로 변환합니다.
- `null` 반환으로 실패를 표현하지 않습니다 → 예외 또는 `Optional`.
- `GlobalExceptionHandler`에서 `BusinessException`, `MethodArgumentNotValidException`, 그 외 `Exception`을 각각 처리하고 공통 에러 응답 포맷으로 변환합니다.
- 예상하지 못한 예외는 `log.error`로 스택트레이스를 남기고, 클라이언트에는 내부 정보를 노출하지 않습니다.
