package org.cambium.common.exception;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum ErrorCodeCommon implements ErrorCode {
    OK(0, "success", HttpStatus.OK),
    UNEXPECTED_SERVER_EXCEPTION(500, "something is not well configured in database");

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCodeCommon(int code, String message) {
        this(code, message, HttpStatus.BAD_REQUEST);
    }

    @Override
    public String getServiceCode() {
        return "cambium.common";
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
}
