

package org.cambium.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;

@RequiredArgsConstructor
public class DefaultErrorCode implements ErrorCode {
    public DefaultErrorCode(HttpStatusCode httpStatus, String message, String serviceCode) {
        this(HttpStatus.valueOf(httpStatus.value()), message, serviceCode);
    }
    public DefaultErrorCode(ResponseStatusException ex) {
        this(ex.getStatusCode(),ex.getMessage(),null);
    }
    @Getter
    private final HttpStatus httpStatus ;
    @Getter
    private final String message ;
    @Getter
    private final String serviceCode ;

    @Override
    public int getCode() {
        return httpStatus.value();
    }
}
