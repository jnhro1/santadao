package com.jnhro1.santadao.event.dto.response;

import com.jnhro1.santadao.event.entity.Event;
import java.time.LocalDateTime;
import java.util.List;

/** 관리 링크 응답: 이름별 PIN. 추첨 결과는 포함하지 않는다. */
public record AdminEventResponse(
        String name,
        String publicCode,
        LocalDateTime createdAt,
        List<ParticipantPinResponse> participants
) {

    public static AdminEventResponse from(Event event) {
        return new AdminEventResponse(
                event.getName(),
                event.getPublicCode(),
                event.getCreatedAt(),
                event.getParticipants().stream().map(ParticipantPinResponse::from).toList()
        );
    }
}
