package com.jnhro1.santadao.global.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

@SpringBootTest
@AutoConfigureMockMvc
class SwaggerConfigTest {

    private static final String REVEAL_RESPONSES = "$.paths['/api/v1/events/{publicCode}/reveal'].post.responses";

    @Autowired
    private MockMvcTester mvc;

    @Test
    @DisplayName("@ApiErrorCodeExample에 지정한 에러가 HTTP 상태별 응답 예시로 문서화된다")
    void apiErrorCodeExamples() {
        MvcTestResult result = mvc.get().uri("/v3/api-docs").exchange();

        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$.info.title").isEqualTo("Santadao Backend");
        assertThat(result).bodyJson()
                .extractingPath(REVEAL_RESPONSES + "['401'].content['application/json'].examples.INVALID_PIN.value")
                .isEqualTo(Map.of(
                        "code", 401,
                        "status", "UNAUTHORIZED",
                        "message", "비밀 코드가 맞지 않아요. 다시 입력해 주세요."));
        // 같은 상태(404)의 예시는 덮어쓰지 않고 함께 들어간다
        assertThat(result).bodyJson()
                .hasPath(REVEAL_RESPONSES + "['404'].content['application/json'].examples.EVENT_NOT_FOUND")
                .hasPath(REVEAL_RESPONSES + "['404'].content['application/json'].examples.PARTICIPANT_NOT_FOUND");
    }

    @Test
    @DisplayName("모든 API에 서버 내부 오류 예시가 들어간다")
    void internalErrorExampleOnEveryOperation() {
        MvcTestResult result = mvc.get().uri("/v3/api-docs").exchange();

        assertThat(result).bodyJson()
                .hasPath("$.paths['/api/v1/admin/events/{adminToken}'].get.responses['500'].content['application/json'].examples.INTERNAL_ERROR")
                .hasPath("$.paths['/api/v1/events'].post.responses['500'].content['application/json'].examples.CODE_GENERATION_FAILED");
    }
}
