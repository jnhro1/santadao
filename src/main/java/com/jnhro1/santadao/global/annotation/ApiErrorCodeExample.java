package com.jnhro1.santadao.global.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.jnhro1.santadao.global.exception.ErrorCode;

/**
 * Swagger 문서에 이 API가 응답할 수 있는 에러 예시를 추가한다.
 * SwaggerConfig가 ErrorCode의 HTTP 상태별로 묶어서 응답 예시를 만든다.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiErrorCodeExample {

    ErrorCode[] value();
}
