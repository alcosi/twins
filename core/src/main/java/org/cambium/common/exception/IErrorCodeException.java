package org.cambium.common.exception;

public interface IErrorCodeException {
    public int getErrorCode();
    public <T extends IErrorCodeException> T setErrorCode(int errorCode);
    public String getErrorMsg();
    public <T extends IErrorCodeException> T setErrorMsg(String errorMsg);
}
