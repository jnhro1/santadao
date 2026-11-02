package com.jnhro1.santadao.event.dto.response;

import com.jnhro1.santadao.event.entity.Event;
import java.util.List;

/** 생성 응답: 공용 코드, 관리 토큰, 이름별 PIN. 추첨 결과는 포함하지 않는다. */
public record CreateEventResponse(
        String name,
        String publicCode,
        String adminToken,
        List<ParticipantPinResponse> participants
) {

    public static CreateEventResponse from(Event event) {
        return new CreateEventResponse(
                event.getName(),
                event.getPublicCode(),
                event.getAdminToken(),
                event.getParticipants().stream().map(ParticipantPinResponse::from).toList()
        );
    }
}
