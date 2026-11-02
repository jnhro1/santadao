package com.jnhro1.santadao.global.response;

import org.springframework.http.HttpStatus;

import com.jnhro1.santadao.global.exception.ErrorCode;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ApiResponse<T> {

    private int code;
    private HttpStatus status;
    private String message;
    private T data;

    public ApiResponse(HttpStatus status, String message, T data) {
        this.code = status.value();
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public ApiResponse(ErrorCode errorCode) {
        this.code = errorCode.getCode().value();
        this.status = errorCode.getCode();
        this.message = errorCode.getMessage();
        this.data = null;
    }

    public static <T> ApiResponse<T> of(HttpStatus httpStatus, String message, T data) {
        return new ApiResponse<>(httpStatus, message, data);
    }

    public static <T> ApiResponse<T> of(HttpStatus httpStatus, T data) {
        return new ApiResponse<>(httpStatus, httpStatus.name(), data);
    }

    public static <T> ApiResponse<T> of(ErrorCode errorCode) {
        return new ApiResponse<>(errorCode);
    }

    public static <T> ApiResponse<T> ok(T data) {
        return of(HttpStatus.OK, HttpStatus.OK.name(), data);
    }

    public static <T> ApiResponse<T> ok() {
        return of(HttpStatus.OK, HttpStatus.OK.name(), null);
    }
}
