package org.cambium.common.exception;


import org.springframework.http.HttpStatus;

import java.util.Hashtable;

public class ServiceException extends ExternalServiceException implements IServiceException, IContextException, IHttpStatusException {
    private Hashtable<String, String> context = new Hashtable<>();
    private String serviceCode;

    public ServiceException(String serviceCode, int errorCode, String errorMsg, HttpStatus httpStatus) {
        super(errorCode, errorMsg);
        this.serviceCode = serviceCode;
        this.httpStatus = httpStatus;
    }

    public ServiceException(ErrorCode serviceError, String message) {
        this(serviceError.getServiceCode(), serviceError.getCode(), message != null ? message : serviceError.getMessage(), serviceError.getHttpStatus());
    }

    public Hashtable<String, String> getContext() {
        return context;
    }

    public ServiceException addContext(String key, String value) {
        if (context == null)
            context = new Hashtable<>();
        context.putIfAbsent(key, value);
        return this;
    }

    @Override
    public String getServiceCode() {
        return null;
    }

    @Override
    public ServiceException setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
        return this;
    }

    private HttpStatus httpStatus;

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    @Override
    public ServiceException setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
        return this;
    }
}
