package com.jnhro1.santadao.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값을 다시 확인해 주세요."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없어요."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "요청을 처리하지 못했어요. 잠시 후 다시 시도해 주세요."),

    // Event
    EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "이벤트를 찾을 수 없어요."),
    DUPLICATE_PARTICIPANT_NAME(HttpStatus.BAD_REQUEST, "참가자 이름이 중복돼요."),
    INVALID_PARTICIPANT_NAME(HttpStatus.BAD_REQUEST, "참가자 이름은 1~10자로 입력해 주세요."),
    CODE_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이벤트 코드를 만들지 못했어요. 다시 시도해 주세요."),

    // Participant
    PARTICIPANT_NOT_FOUND(HttpStatus.NOT_FOUND, "참가자를 찾을 수 없어요."),
    INVALID_PIN(HttpStatus.UNAUTHORIZED, "비밀 코드가 맞지 않아요. 다시 입력해 주세요.");

    /** 응답 HTTP 상태 (ApiResponse의 code/status로 그대로 쓰인다) */
    private final HttpStatus code;
    private final String message;
}
