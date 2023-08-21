package org.cambium.common.exception;

import org.springframework.http.HttpStatus;

public interface IServiceException {
    public String getServiceCode();

    public <T extends IServiceException> T setServiceCode(String serviceCode);
}
