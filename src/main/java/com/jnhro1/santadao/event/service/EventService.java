package com.jnhro1.santadao.event.service;

import com.jnhro1.santadao.event.dto.request.CreateEventRequest;
import com.jnhro1.santadao.event.dto.request.RevealRequest;
import com.jnhro1.santadao.event.dto.response.AdminEventResponse;
import com.jnhro1.santadao.event.dto.response.CreateEventResponse;
import com.jnhro1.santadao.event.dto.response.EventPublicResponse;
import com.jnhro1.santadao.event.dto.response.RevealResponse;
import com.jnhro1.santadao.event.entity.Event;
import com.jnhro1.santadao.event.entity.Participant;
import com.jnhro1.santadao.event.repository.EventRepository;
import com.jnhro1.santadao.global.exception.BadRequestException;
import com.jnhro1.santadao.global.exception.BusinessException;
import com.jnhro1.santadao.global.exception.ErrorCode;
import com.jnhro1.santadao.global.exception.NotFoundException;
import com.jnhro1.santadao.global.exception.UnauthorizedException;
import com.jnhro1.santadao.global.util.RandomCodeGenerator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

    private static final int MAX_CODE_GENERATION_ATTEMPTS = 5;

    private final EventRepository eventRepository;
    private final ManitoDrawer manitoDrawer;
    private final RandomCodeGenerator randomCodeGenerator;

    @Transactional
    public CreateEventResponse createEvent(CreateEventRequest request) {
        List<String> names = normalizeNames(request.participants());

        Event event = Event.builder()
                .name(request.name().trim())
                .memo(trimToNull(request.memo()))
                .publicCode(generateUniqueCode(Event.PUBLIC_CODE_LENGTH, eventRepository::existsByPublicCode))
                .adminToken(generateUniqueCode(Event.ADMIN_TOKEN_LENGTH, eventRepository::existsByAdminToken))
                .build();
        issuePins(names).forEach((name, pin) ->
                event.addParticipant(Participant.builder().name(name).pin(pin).build()));
        eventRepository.save(event);

        assignManitos(event.getParticipants());
        return CreateEventResponse.from(event);
    }

    public EventPublicResponse getPublicEvent(String publicCode) {
        return EventPublicResponse.from(findEventByPublicCode(publicCode));
    }

    public RevealResponse revealManito(String publicCode, RevealRequest request) {
        Event event = findEventByPublicCode(publicCode);
        Participant participant = event.findParticipant(request.participantId(), trimToNull(request.name()))
                .orElseThrow(() -> new NotFoundException(ErrorCode.PARTICIPANT_NOT_FOUND));
        if (!participant.matchesPin(request.pin())) {
            throw new UnauthorizedException(ErrorCode.INVALID_PIN);
        }
        return new RevealResponse(participant.getManito().getName());
    }

    public AdminEventResponse getAdminEvent(String adminToken) {
        Event event = eventRepository.findWithParticipantsByAdminToken(adminToken)
                .orElseThrow(() -> new NotFoundException(ErrorCode.EVENT_NOT_FOUND));
        return AdminEventResponse.from(event);
    }

    private Event findEventByPublicCode(String publicCode) {
        return eventRepository.findWithParticipantsByPublicCode(publicCode)
                .orElseThrow(() -> new NotFoundException(ErrorCode.EVENT_NOT_FOUND));
    }

    /** 앞뒤 공백 제거 후 길이 검사, 대소문자 무시 중복 검사 */
    private List<String> normalizeNames(List<String> rawNames) {
        List<String> names = rawNames.stream().map(String::trim).toList();
        Set<String> seen = new HashSet<>();
        for (String name : names) {
            boolean isValidLength = !name.isEmpty() && name.length() <= Participant.NAME_MAX_LENGTH;
            if (!isValidLength) {
                throw new BadRequestException(ErrorCode.INVALID_PARTICIPANT_NAME);
            }
            if (!seen.add(name.toLowerCase(Locale.ROOT))) {
                throw new BadRequestException(ErrorCode.DUPLICATE_PARTICIPANT_NAME,
                        "‘" + name + "’ 이름이 중복돼요. 구분되게 다시 입력해 주세요");
            }
        }
        return names;
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    /** 이벤트 안에서 겹치지 않는 PIN을 이름 순서대로 발급한다. */
    private Map<String, String> issuePins(List<String> names) {
        Map<String, String> pinsByName = new LinkedHashMap<>();
        Set<String> issuedPins = new HashSet<>();
        for (String name : names) {
            String pin;
            do {
                pin = randomCodeGenerator.pin();
            } while (!issuedPins.add(pin));
            pinsByName.put(name, pin);
        }
        return pinsByName;
    }

    private String generateUniqueCode(int length, Predicate<String> exists) {
        for (int attempt = 0; attempt < MAX_CODE_GENERATION_ATTEMPTS; attempt++) {
            String code = randomCodeGenerator.alphanumeric(length);
            if (!exists.test(code)) {
                return code;
            }
        }
        throw new BusinessException(ErrorCode.CODE_GENERATION_FAILED);
    }

    private void assignManitos(List<Participant> participants) {
        List<Integer> assignment = manitoDrawer.draw(participants.size());
        for (int i = 0; i < participants.size(); i++) {
            participants.get(i).assignManito(participants.get(assignment.get(i)));
        }
    }
}
