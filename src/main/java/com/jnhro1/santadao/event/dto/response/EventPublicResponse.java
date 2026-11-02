package com.jnhro1.santadao.event.dto.response;

import com.jnhro1.santadao.event.entity.Event;
import java.util.List;

/** 공용 링크 응답: 이벤트 정보와 참가자 이름만. PIN과 추첨 결과는 포함하지 않는다. */
public record EventPublicResponse(String name, String memo, List<ParticipantNameResponse> participants) {

    public static EventPublicResponse from(Event event) {
        return new EventPublicResponse(
                event.getName(),
                event.getMemo(),
                event.getParticipants().stream().map(ParticipantNameResponse::from).toList()
        );
    }
}
