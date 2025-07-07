package org.cambium.common.exception;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum ErrorCodeCommon implements ErrorCode {
    OK(0, "success", HttpStatus.OK),
    UNEXPECTED_SERVER_EXCEPTION(500, "unxpected server exception", HttpStatus.INTERNAL_SERVER_ERROR),
    NOT_IMPLEMENTED(501, "this operation is not supported yet"),
    FORBIDDEN(502, "this operation is forbidden"),
    UUID_UNKNOWN(10000, "uuid is unknown", HttpStatus.NOT_FOUND),
    UUID_ALREADY_EXIST(10001, "uuid is already exist"),
    ENTITY_INVALID(10002, "entity invalid"),
    FEATURER_IS_NULL(600, "Got featurer is null"),
    FEATURER_ID_UNKNOWN(601, "featurer id is unknown"),
    FEATURER_INCORRECT_TYPE(602, "featurer type is incorrect"),
    FEATURER_WRONG_PARAMS(604, "featurer type is incorrect"),
    FEATURER_WITHOUT_SERIALIZATION(605, "featurer has no serialization"),;

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
