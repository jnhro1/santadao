package com.jnhro1.santadao.event.entity;

import com.jnhro1.santadao.global.common.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "events", comment = "마니또 이벤트")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Event extends BaseEntity {

    public static final int NAME_MAX_LENGTH = 30;
    public static final int MEMO_MAX_LENGTH = 200;
    public static final int PUBLIC_CODE_LENGTH = 8;
    public static final int ADMIN_TOKEN_LENGTH = 32;

    @Column(nullable = false, length = NAME_MAX_LENGTH, comment = "이벤트 이름")
    private String name;

    @Column(length = MEMO_MAX_LENGTH, comment = "메모 (선물 금액 등)")
    private String memo;

    @Column(nullable = false, unique = true, length = PUBLIC_CODE_LENGTH, comment = "공용 링크 코드")
    private String publicCode;

    @Column(nullable = false, unique = true, length = ADMIN_TOKEN_LENGTH, comment = "주최자 관리 링크 토큰")
    private String adminToken;

    @OrderBy("id")
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Participant> participants = new ArrayList<>();

    @Builder
    private Event(String name, String memo, String publicCode, String adminToken) {
        this.name = name;
        this.memo = memo;
        this.publicCode = publicCode;
        this.adminToken = adminToken;
    }

    public void addParticipant(Participant participant) {
        participants.add(participant);
        participant.assignEvent(this);
    }

    public List<Participant> getParticipants() {
        return Collections.unmodifiableList(participants);
    }

    public Optional<Participant> findParticipant(Long participantId, String name) {
        return participants.stream()
                .filter(participant -> participantId != null
                        ? participant.getId().equals(participantId)
                        : participant.getName().equals(name))
                .findFirst();
    }
}
