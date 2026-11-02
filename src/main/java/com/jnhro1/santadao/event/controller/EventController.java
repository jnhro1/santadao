package com.jnhro1.santadao.event.controller;

import com.jnhro1.santadao.event.dto.request.CreateEventRequest;
import com.jnhro1.santadao.event.dto.request.RevealRequest;
import com.jnhro1.santadao.event.dto.response.CreateEventResponse;
import com.jnhro1.santadao.event.dto.response.EventPublicResponse;
import com.jnhro1.santadao.event.dto.response.RevealResponse;
import com.jnhro1.santadao.event.service.EventService;
import com.jnhro1.santadao.global.annotation.ApiErrorCodeExample;
import com.jnhro1.santadao.global.exception.ErrorCode;
import com.jnhro1.santadao.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Event", description = "마니또 이벤트 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/events")
public class EventController {

    private final EventService eventService;

    @Operation(summary = "이벤트 생성 및 추첨", description = "이벤트를 만들고 즉시 추첨합니다. 공용 코드, 관리 토큰, 이름별 비밀 코드를 반환합니다.")
    @ApiErrorCodeExample({
            ErrorCode.INVALID_INPUT,
            ErrorCode.DUPLICATE_PARTICIPANT_NAME,
            ErrorCode.INVALID_PARTICIPANT_NAME,
            ErrorCode.CODE_GENERATION_FAILED
    })
    @PostMapping
    public ResponseEntity<ApiResponse<CreateEventResponse>> createEvent(
            @Valid @RequestBody CreateEventRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(HttpStatus.CREATED, eventService.createEvent(request)));
    }

    @Operation(summary = "공용 이벤트 조회", description = "이벤트 이름, 메모, 참가자 이름 목록을 반환합니다. 비밀 코드와 결과는 포함하지 않습니다.")
    @ApiErrorCodeExample(ErrorCode.EVENT_NOT_FOUND)
    @GetMapping("/{publicCode}")
    public ApiResponse<EventPublicResponse> getPublicEvent(@PathVariable String publicCode) {
        return ApiResponse.ok(eventService.getPublicEvent(publicCode));
    }

    @Operation(summary = "내 마니또 확인", description = "참가자와 비밀 코드가 일치하면 마니또 이름을 반환합니다.")
    @ApiErrorCodeExample({
            ErrorCode.INVALID_INPUT,
            ErrorCode.INVALID_PIN,
            ErrorCode.EVENT_NOT_FOUND,
            ErrorCode.PARTICIPANT_NOT_FOUND
    })
    @PostMapping("/{publicCode}/reveal")
    public ApiResponse<RevealResponse> revealManito(
            @PathVariable String publicCode,
            @Valid @RequestBody RevealRequest request) {
        return ApiResponse.ok(eventService.revealManito(publicCode, request));
    }
}
