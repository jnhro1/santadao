package com.jnhro1.santadao.event.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import com.jnhro1.santadao.event.dto.request.RevealRequest;
import com.jnhro1.santadao.event.dto.response.AdminEventResponse;
import com.jnhro1.santadao.event.dto.response.CreateEventResponse;
import com.jnhro1.santadao.event.dto.response.EventPublicResponse;
import com.jnhro1.santadao.event.dto.response.ParticipantNameResponse;
import com.jnhro1.santadao.event.dto.response.ParticipantPinResponse;
import com.jnhro1.santadao.event.dto.response.RevealResponse;
import com.jnhro1.santadao.event.service.EventService;
import com.jnhro1.santadao.global.config.SecurityConfig;
import com.jnhro1.santadao.global.exception.ErrorCode;
import com.jnhro1.santadao.global.exception.NotFoundException;
import com.jnhro1.santadao.global.exception.UnauthorizedException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

@WebMvcTest(controllers = {EventController.class, AdminEventController.class})
@Import(SecurityConfig.class)
class EventControllerTest {

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private EventService eventService;

    @Test
    @DisplayName("이벤트 생성: 201과 함께 화면이 읽는 필드(publicCode, adminToken, participants[].pin)를 반환한다")
    void createEvent() {
        given(eventService.createEvent(any())).willReturn(new CreateEventResponse(
                "모임", "K7mQ2xPa", "a".repeat(32),
                List.of(new ParticipantPinResponse(1L, "민수", "0123"))));

        MvcTestResult result = post("/api/v1/events", """
                {"name": "모임", "memo": null, "participants": ["민수", "지영", "철수"]}
                """);

        assertThat(result).hasStatus(HttpStatus.CREATED);
        assertThat(result).bodyJson().extractingPath("$.code").isEqualTo(201);
        assertThat(result).bodyJson().extractingPath("$.status").isEqualTo("CREATED");
        assertThat(result).bodyJson().extractingPath("$.data.publicCode").isEqualTo("K7mQ2xPa");
        assertThat(result).bodyJson().extractingPath("$.data.adminToken").isEqualTo("a".repeat(32));
        assertThat(result).bodyJson().extractingPath("$.data.participants[0].pin").isEqualTo("0123");
    }

    @Test
    @DisplayName("이벤트 이름이 비어 있으면 400과 한국어 에러 메시지를 반환한다")
    void createEvent_blankName() {
        MvcTestResult result = post("/api/v1/events", """
                {"name": " ", "participants": ["민수", "지영", "철수"]}
                """);

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(result).bodyJson().extractingPath("$.code").isEqualTo(400);
        assertThat(result).bodyJson().extractingPath("$.message").isEqualTo("이벤트 이름을 입력해 주세요");
    }

    @Test
    @DisplayName("이벤트 이름 30자 초과, 메모 200자 초과는 400")
    void createEvent_tooLong() {
        assertThat(post("/api/v1/events", """
                {"name": "%s", "participants": ["a", "b", "c"]}
                """.formatted("가".repeat(31)))).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(post("/api/v1/events", """
                {"name": "모임", "memo": "%s", "participants": ["a", "b", "c"]}
                """.formatted("가".repeat(201)))).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("참가자가 3명 미만이거나 20명 초과면 400")
    void createEvent_participantCount() {
        assertThat(post("/api/v1/events", """
                {"name": "모임", "participants": ["a", "b"]}
                """)).hasStatus(HttpStatus.BAD_REQUEST);

        String twentyOne = String.join(",", java.util.stream.IntStream.rangeClosed(1, 21)
                .mapToObj(i -> "\"p" + i + "\"").toList());
        assertThat(post("/api/v1/events", """
                {"name": "모임", "participants": [%s]}
                """.formatted(twentyOne))).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("공용 조회 응답에는 PIN과 결과가 포함되지 않는다")
    void getPublicEvent() {
        given(eventService.getPublicEvent("K7mQ2xPa")).willReturn(new EventPublicResponse(
                "모임", "메모", List.of(new ParticipantNameResponse(1L, "민수"))));

        MvcTestResult result = mvc.get().uri("/api/v1/events/K7mQ2xPa").exchange();

        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$.data.participants[0].name").isEqualTo("민수");
        assertThat(result).bodyJson().doesNotHavePath("$.data.participants[0].pin");
        assertThat(result).bodyJson().doesNotHavePath("$.data.participants[0].manito");
    }

    @Test
    @DisplayName("없는 이벤트는 404")
    void getPublicEvent_notFound() {
        given(eventService.getPublicEvent("NOPE0000")).willThrow(new NotFoundException(ErrorCode.EVENT_NOT_FOUND));

        MvcTestResult result = mvc.get().uri("/api/v1/events/NOPE0000").exchange();

        assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
        assertThat(result).bodyJson().extractingPath("$.message").isEqualTo(ErrorCode.EVENT_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("결과 확인: 올바른 PIN이면 manitoName을 반환한다")
    void revealManito() {
        given(eventService.revealManito(eq("K7mQ2xPa"), any(RevealRequest.class)))
                .willReturn(new RevealResponse("지영"));

        MvcTestResult result = post("/api/v1/events/K7mQ2xPa/reveal", """
                {"participantId": 1, "name": "민수", "pin": "0123"}
                """);

        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$.code").isEqualTo(200);
        assertThat(result).bodyJson().extractingPath("$.message").isEqualTo("OK");
        assertThat(result).bodyJson().extractingPath("$.data.manitoName").isEqualTo("지영");
    }

    @Test
    @DisplayName("결과 확인: PIN이 틀리면 401")
    void revealManito_wrongPin() {
        given(eventService.revealManito(eq("K7mQ2xPa"), any(RevealRequest.class)))
                .willThrow(new UnauthorizedException(ErrorCode.INVALID_PIN));

        MvcTestResult result = post("/api/v1/events/K7mQ2xPa/reveal", """
                {"participantId": 1, "pin": "9999"}
                """);

        assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
        assertThat(result).bodyJson().extractingPath("$.message").isEqualTo(ErrorCode.INVALID_PIN.getMessage());
    }

    @Test
    @DisplayName("결과 확인: PIN 형식이 4자리 숫자가 아니면 400")
    void revealManito_invalidPinFormat() {
        MvcTestResult result = post("/api/v1/events/K7mQ2xPa/reveal", """
                {"participantId": 1, "pin": "12a"}
                """);

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("관리 조회: 이름별 PIN과 생성 시각을 반환하고 결과는 포함하지 않는다")
    void getAdminEvent() {
        given(eventService.getAdminEvent("a".repeat(32))).willReturn(new AdminEventResponse(
                "모임", "K7mQ2xPa", LocalDateTime.of(2026, 12, 1, 10, 0),
                List.of(new ParticipantPinResponse(1L, "민수", "0123"))));

        MvcTestResult result = mvc.get().uri("/api/v1/admin/events/" + "a".repeat(32)).exchange();

        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$.data.publicCode").isEqualTo("K7mQ2xPa");
        assertThat(result).bodyJson().extractingPath("$.data.participants[0].pin").isEqualTo("0123");
        assertThat(result).bodyJson().extractingPath("$.data.createdAt").isNotNull();
        assertThat(result).bodyJson().doesNotHavePath("$.data.participants[0].manito");
    }

    private MvcTestResult post(String uri, String json) {
        return mvc.post().uri(uri).contentType(MediaType.APPLICATION_JSON).content(json).exchange();
    }
}
