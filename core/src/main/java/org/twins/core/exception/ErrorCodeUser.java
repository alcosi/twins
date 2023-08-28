package org.twins.core.exception;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

import java.util.Arrays;

@RequiredArgsConstructor
public enum ErrorCodeUser implements ErrorCode {
    INCORRECT_UUID(1000, "unknown user"),
    UNKNOWN_USER(1001, "unknown user"),
    UNKNOWN_BUSINESS_ACCOUNT(1002, "unknown business_account"),
    UNKNOWN_DOMAIN(1003, "unknown domain");

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCodeUser(int code, String message) {
        this(code, message, HttpStatus.BAD_REQUEST);
    }

    @Override
    public String getServiceCode() {
        return "TWINS_USER";
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public static ErrorCode byCode(int code) {
        return Arrays.stream(values()).filter(e -> e.code == code).findAny().orElse(null);
    }
}
