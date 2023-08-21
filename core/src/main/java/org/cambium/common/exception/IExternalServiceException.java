package org.cambium.common.exception;

public interface IExternalServiceException {
    public String getErrorCodeExternal();
    public <T extends IExternalServiceException> T setErrorCodeExternal(String errorCodeExternal);
    public String getErrorMsgExternal();
    public <T extends IExternalServiceException> T setErrorMsgExternal(String errorMsgExternal);
}
