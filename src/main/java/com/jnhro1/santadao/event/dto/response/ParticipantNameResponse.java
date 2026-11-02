package com.jnhro1.santadao.event.dto.response;

import com.jnhro1.santadao.event.entity.Participant;

public record ParticipantNameResponse(Long id, String name) {

    public static ParticipantNameResponse from(Participant participant) {
        return new ParticipantNameResponse(participant.getId(), participant.getName());
    }
}
