package com.jnhro1.santadao.event.controller;

import com.jnhro1.santadao.event.dto.response.AdminEventResponse;
import com.jnhro1.santadao.event.service.EventService;
import com.jnhro1.santadao.global.annotation.ApiErrorCodeExample;
import com.jnhro1.santadao.global.exception.ErrorCode;
import com.jnhro1.santadao.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Event", description = "주최자 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/events")
public class AdminEventController {

    private final EventService eventService;

    @Operation(summary = "관리 정보 조회", description = "이름별 비밀 코드 목록을 반환합니다. 추첨 결과는 포함하지 않습니다.")
    @ApiErrorCodeExample(ErrorCode.EVENT_NOT_FOUND)
    @GetMapping("/{adminToken}")
    public ApiResponse<AdminEventResponse> getAdminEvent(@PathVariable String adminToken) {
        return ApiResponse.ok(eventService.getAdminEvent(adminToken));
    }
}
