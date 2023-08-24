package org.cambium.common.exception;

public interface IServiceException {
    public String getServiceCode();

    public <T extends IServiceException> T setServiceCode(String serviceCode);
}
