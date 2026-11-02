package com.jnhro1.santadao.event.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.jnhro1.santadao.event.dto.request.CreateEventRequest;
import com.jnhro1.santadao.event.dto.request.RevealRequest;
import com.jnhro1.santadao.event.dto.response.AdminEventResponse;
import com.jnhro1.santadao.event.dto.response.CreateEventResponse;
import com.jnhro1.santadao.event.dto.response.EventPublicResponse;
import com.jnhro1.santadao.event.dto.response.ParticipantNameResponse;
import com.jnhro1.santadao.event.dto.response.ParticipantPinResponse;
import com.jnhro1.santadao.event.entity.Event;
import com.jnhro1.santadao.event.entity.Participant;
import com.jnhro1.santadao.event.repository.EventRepository;
import com.jnhro1.santadao.global.exception.BusinessException;
import com.jnhro1.santadao.global.exception.ErrorCode;
import jakarta.persistence.EntityManager;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class EventServiceTest {

    @Autowired
    private EventService eventService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("이벤트를 만들면 공용 코드, 관리 토큰, 이름별 4자리 PIN이 발급되고 추첨이 확정된다")
    void createEvent() {
        // when
        CreateEventResponse response = eventService.createEvent(
                request("2026 개발팀 마니또", "3만원 이하", List.of(" 민수 ", "지영", "철수", "영희")));
        flushAndClear();

        // then
        assertThat(response.name()).isEqualTo("2026 개발팀 마니또");
        assertThat(response.publicCode()).hasSize(Event.PUBLIC_CODE_LENGTH);
        assertThat(response.adminToken()).hasSize(Event.ADMIN_TOKEN_LENGTH);
        assertThat(response.participants()).extracting(ParticipantPinResponse::name)
                .containsExactly("민수", "지영", "철수", "영희");
        assertThat(response.participants()).extracting(ParticipantPinResponse::pin)
                .allMatch(pin -> pin.matches("\\d{4}"))
                .doesNotHaveDuplicates();

        Event saved = eventRepository.findWithParticipantsByPublicCode(response.publicCode()).orElseThrow();
        List<Participant> participants = saved.getParticipants();
        Set<Long> receivers = new HashSet<>();
        for (Participant participant : participants) {
            assertThat(participant.getManito()).isNotNull();
            assertThat(participant.getManito().getId()).isNotEqualTo(participant.getId());
            receivers.add(participant.getManito().getId());
        }
        assertThat(receivers).hasSize(participants.size());
    }

    @Test
    @DisplayName("메모가 공백이면 null로 저장된다")
    void createEvent_blankMemo() {
        CreateEventResponse response = eventService.createEvent(request("모임", "  ", List.of("a", "b", "c")));

        EventPublicResponse publicEvent = eventService.getPublicEvent(response.publicCode());
        assertThat(publicEvent.memo()).isNull();
    }

    @Test
    @DisplayName("공백을 지우고 대소문자를 무시했을 때 중복된 이름이 있으면 거부된다")
    void createEvent_duplicateName() {
        assertThatThrownBy(() -> eventService.createEvent(request("모임", null, List.of("Kim", " kim ", "Lee"))))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("kim")
                .extracting("errorCode").isEqualTo(ErrorCode.DUPLICATE_PARTICIPANT_NAME);
    }

    @Test
    @DisplayName("공백을 지운 이름이 비어 있거나 10자를 넘으면 거부된다")
    void createEvent_invalidName() {
        assertThatThrownBy(() -> eventService.createEvent(request("모임", null, List.of("가나다라마바사아자차카", "b", "c"))))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_PARTICIPANT_NAME);
        assertThatThrownBy(() -> eventService.createEvent(request("모임", null, List.of("   ", "b", "c"))))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_PARTICIPANT_NAME);
    }

    @Test
    @DisplayName("공용 조회는 이름 목록만 반환한다")
    void getPublicEvent() {
        CreateEventResponse created = eventService.createEvent(request("모임", "메모", List.of("a", "b", "c")));
        flushAndClear();

        EventPublicResponse response = eventService.getPublicEvent(created.publicCode());

        assertThat(response.name()).isEqualTo("모임");
        assertThat(response.memo()).isEqualTo("메모");
        assertThat(response.participants()).extracting(ParticipantNameResponse::name).containsExactly("a", "b", "c");
    }

    @Test
    @DisplayName("없는 공용 코드는 EVENT_NOT_FOUND")
    void getPublicEvent_notFound() {
        assertThatThrownBy(() -> eventService.getPublicEvent("NOPE0000"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.EVENT_NOT_FOUND);
    }

    @Test
    @DisplayName("공용 코드는 대소문자를 구분한다 (대소문자만 다른 코드로는 조회되지 않는다)")
    void getPublicEvent_caseSensitive() {
        CreateEventResponse created = eventService.createEvent(request("모임", null, List.of("a", "b", "c")));
        String swappedCase = swapCase(created.publicCode());
        if (swappedCase.equals(created.publicCode())) {
            return; // 숫자로만 이뤄진 코드 (확률 극히 낮음)
        }

        assertThatThrownBy(() -> eventService.getPublicEvent(swappedCase))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.EVENT_NOT_FOUND);
    }

    @Test
    @DisplayName("참가자 id와 올바른 PIN이면 자기 마니또 이름을 반환한다")
    void revealManito_byId() {
        CreateEventResponse created = eventService.createEvent(request("모임", null, List.of("a", "b", "c")));
        flushAndClear();
        ParticipantPinResponse me = created.participants().get(0);

        String manitoName = eventService.revealManito(created.publicCode(),
                new RevealRequest(me.id(), null, me.pin())).manitoName();

        assertThat(manitoName).isIn("b", "c");
    }

    @Test
    @DisplayName("id 없이 이름과 PIN으로도 확인할 수 있다")
    void revealManito_byName() {
        CreateEventResponse created = eventService.createEvent(request("모임", null, List.of("a", "b", "c")));
        flushAndClear();
        ParticipantPinResponse me = created.participants().get(1);

        String manitoName = eventService.revealManito(created.publicCode(),
                new RevealRequest(null, "b", me.pin())).manitoName();

        assertThat(manitoName).isIn("a", "c");
    }

    @Test
    @DisplayName("PIN이 틀리면 INVALID_PIN")
    void revealManito_wrongPin() {
        CreateEventResponse created = eventService.createEvent(request("모임", null, List.of("a", "b", "c")));
        ParticipantPinResponse me = created.participants().get(0);
        String wrongPin = created.participants().get(1).pin();

        assertThatThrownBy(() -> eventService.revealManito(created.publicCode(),
                new RevealRequest(me.id(), null, wrongPin)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_PIN);
    }

    @Test
    @DisplayName("다른 이벤트의 참가자 id로는 확인할 수 없다")
    void revealManito_participantOfOtherEvent() {
        CreateEventResponse mine = eventService.createEvent(request("모임1", null, List.of("a", "b", "c")));
        CreateEventResponse other = eventService.createEvent(request("모임2", null, List.of("x", "y", "z")));
        ParticipantPinResponse outsider = other.participants().get(0);

        assertThatThrownBy(() -> eventService.revealManito(mine.publicCode(),
                new RevealRequest(outsider.id(), null, outsider.pin())))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.PARTICIPANT_NOT_FOUND);
    }

    @Test
    @DisplayName("관리 토큰으로 이름별 PIN을 다시 조회할 수 있다")
    void getAdminEvent() {
        CreateEventResponse created = eventService.createEvent(request("모임", null, List.of("a", "b", "c")));
        flushAndClear();

        AdminEventResponse response = eventService.getAdminEvent(created.adminToken());

        assertThat(response.publicCode()).isEqualTo(created.publicCode());
        assertThat(response.createdAt()).isNotNull();
        assertThat(response.participants()).containsExactlyElementsOf(created.participants());
    }

    @Test
    @DisplayName("공용 코드로는 관리 정보를 조회할 수 없다")
    void getAdminEvent_withPublicCode() {
        CreateEventResponse created = eventService.createEvent(request("모임", null, List.of("a", "b", "c")));

        assertThatThrownBy(() -> eventService.getAdminEvent(created.publicCode()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.EVENT_NOT_FOUND);
    }

    private CreateEventRequest request(String name, String memo, List<String> participants) {
        return new CreateEventRequest(name, memo, participants);
    }

    private String swapCase(String value) {
        StringBuilder swapped = new StringBuilder(value.length());
        for (char c : value.toCharArray()) {
            swapped.append(Character.isUpperCase(c) ? Character.toLowerCase(c) : Character.toUpperCase(c));
        }
        return swapped.toString();
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
