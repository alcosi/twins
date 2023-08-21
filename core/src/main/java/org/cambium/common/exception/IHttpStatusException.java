package org.cambium.common.exception;

import org.springframework.http.HttpStatus;

public interface IHttpStatusException {
    public HttpStatus getHttpStatus();

    public <T extends IHttpStatusException> T setHttpStatus(HttpStatus httpStatus);
}
