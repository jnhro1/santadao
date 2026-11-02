package com.jnhro1.santadao.event.entity;

import com.jnhro1.santadao.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "participants",
        comment = "마니또 참가자",
        uniqueConstraints = @UniqueConstraint(name = "uk_participants_event_name", columnNames = {"event_id", "name"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Participant extends BaseEntity {

    public static final int NAME_MAX_LENGTH = 10;
    public static final int PIN_LENGTH = 4;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT),
            comment = "이벤트 ID (events.id)")
    private Event event;

    @Column(nullable = false, length = NAME_MAX_LENGTH, comment = "참가자 이름")
    private String name;

    @Column(nullable = false, length = PIN_LENGTH, comment = "결과 확인용 PIN (숫자 4자리)")
    private String pin;

    /** 이 참가자가 선물할 상대. 참가자 저장 후 추첨 단계에서 배정된다. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manito_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT),
            comment = "선물할 상대 참가자 ID (participants.id)")
    private Participant manito;

    @Builder
    private Participant(String name, String pin) {
        this.name = name;
        this.pin = pin;
    }

    void assignEvent(Event event) {
        this.event = event;
    }

    public void assignManito(Participant manito) {
        if (manito == this) {
            throw new IllegalArgumentException("자기 자신을 마니또로 배정할 수 없습니다.");
        }
        this.manito = manito;
    }

    public boolean matchesPin(String pin) {
        return this.pin.equals(pin);
    }
}
