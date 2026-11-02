package com.jnhro1.santadao.global.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;

import com.jnhro1.santadao.global.annotation.ApiErrorCodeExample;
import com.jnhro1.santadao.global.exception.ErrorCode;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.servers.Server;

/**
 * Swagger(OpenAPI) 설정.
 * 로그인 없는 서비스라 인증 스킴은 등록하지 않는다.
 * 모든 API에 INTERNAL_ERROR 예시를, @ApiErrorCodeExample이 붙은 API에는 지정한 에러 예시를 추가한다.
 */
@Configuration
public class SwaggerConfig {

    /** 비어 있으면 요청한 주소를 기준으로 서버 URL이 자동 설정된다. */
    @Value("${springdoc.swagger-ui.server-url:}")
    private String serverUrl;

    @Bean
    public OpenAPI openAPI() {
        OpenAPI openAPI = new OpenAPI().info(apiInfo());
        if (StringUtils.hasText(serverUrl)) {
            openAPI.addServersItem(new Server().url(serverUrl));
        }
        return openAPI;
    }

    @Bean
    public OperationCustomizer errorCodeExampleCustomizer() {
        return (Operation operation, HandlerMethod handlerMethod) -> {
            // 같은 HTTP 상태의 예시가 서로 덮어쓰지 않도록 한 번에 모아서 생성한다
            Set<ErrorCode> errorCodes = new LinkedHashSet<>();
            errorCodes.add(ErrorCode.INTERNAL_ERROR);

            ApiErrorCodeExample apiErrorCodeExample = handlerMethod.getMethodAnnotation(ApiErrorCodeExample.class);
            if (apiErrorCodeExample != null) {
                errorCodes.addAll(List.of(apiErrorCodeExample.value()));
            }

            generateErrorCodeResponseExample(operation, errorCodes);
            return operation;
        };
    }

    private Info apiInfo() {
        return new Info()
                .title("Santadao Backend")
                .description("크리스마스 마니또 랜덤 추첨 서비스 산타다오 API 문서입니다.")
                .version("v1");
    }

    private void generateErrorCodeResponseExample(Operation operation, Set<ErrorCode> errorCodes) {
        Map<Integer, List<ErrorCode>> errorCodesByStatus = errorCodes.stream()
                .collect(Collectors.groupingBy(
                        errorCode -> errorCode.getCode().value(),
                        TreeMap::new,
                        Collectors.toCollection(ArrayList::new)));

        ApiResponses responses = operation.getResponses();
        errorCodesByStatus.forEach((status, codes) -> responses.addApiResponse(
                String.valueOf(status), createErrorApiResponse(status, codes)));
    }

    private ApiResponse createErrorApiResponse(int status, List<ErrorCode> errorCodes) {
        MediaType mediaType = new MediaType();
        errorCodes.forEach(errorCode -> mediaType.addExamples(errorCode.name(), getSwaggerExample(errorCode)));

        return new ApiResponse()
                .description(getDescription(status))
                .content(new Content().addMediaType("application/json", mediaType));
    }

    /**
     * 실제 에러 응답(ApiResponse)과 같은 형태로 예시를 만든다.
     * Swagger 문서 직렬화기는 앱의 Jackson 설정을 따르지 않아 HttpStatus를 "401 UNAUTHORIZED"로 쓰므로,
     * status는 name() 문자열로 직접 넣는다. (data는 항상 null이라 문서에서는 생략된다)
     */
    private Example getSwaggerExample(ErrorCode errorCode) {
        Map<String, Object> errorResponse = new LinkedHashMap<>();
        errorResponse.put("code", errorCode.getCode().value());
        errorResponse.put("status", errorCode.getCode().name());
        errorResponse.put("message", errorCode.getMessage());
        return new Example()
                .summary(errorCode.getMessage())
                .value(errorResponse);
    }

    private String getDescription(int status) {
        HttpStatus httpStatus = HttpStatus.resolve(status);
        if (httpStatus == null) {
            return "Unexpected Error";
        }
        return switch (httpStatus) {
            case BAD_REQUEST -> "잘못된 요청";
            case UNAUTHORIZED -> "인증 실패";
            case FORBIDDEN -> "권한 없음";
            case NOT_FOUND -> "리소스를 찾을 수 없음";
            case CONFLICT -> "요청 충돌";
            case INTERNAL_SERVER_ERROR -> "서버 내부 오류";
            default -> httpStatus.getReasonPhrase();
        };
    }
}
