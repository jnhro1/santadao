package com.jnhro1.santadao.event.dto.response;

import com.jnhro1.santadao.event.entity.Participant;

public record ParticipantPinResponse(Long id, String name, String pin) {

    public static ParticipantPinResponse from(Participant participant) {
        return new ParticipantPinResponse(participant.getId(), participant.getName(), participant.getPin());
    }
}
