package org.cambium.common.exception;

import org.apache.commons.lang3.StringUtils;

public class ExternalServiceException extends SimpleLogException implements IExternalServiceException, IErrorCodeException {
    private int errorCode;
    private String errorMsg;

    private String errorCodeExternal;
    private String errorMsgExternal;

    public ExternalServiceException(int errorCode, String errorMsg) {
        super(errorMsg);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }

    @Override
    public ExternalServiceException setErrorCode(int errorCode) {
        this.errorCode = errorCode;
        return this;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    @Override
    public ExternalServiceException setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
        return this;
    }

    public String getErrorCodeExternal() {
        return errorCodeExternal;
    }

    @Override
    public ExternalServiceException setErrorCodeExternal(String errorCodeExternal) {
        this.errorCodeExternal = errorCodeExternal != null ? errorCodeExternal.toString() : null;
        return this;
    }

    public String getErrorMsgExternal() {
        return errorMsgExternal;
    }

    @Override
    public ExternalServiceException setErrorMsgExternal(String errorMsgExternal) {
        this.errorMsgExternal = errorMsgExternal;
        return this;
    }

    public String getErrorLocation() {
        StackTraceElement ste = this.getStackTrace()[0];
        return "Exception : " + this.getMessage() + " : " + ste.getFileName() + ":" + ste.getLineNumber();

    }

    @Override
    public String toString() {
        return "Exception[" + errorCode + (StringUtils.isNotBlank(errorCodeExternal) ? "|" + errorCodeExternal : "") + "]" + (StringUtils.isNotBlank(errorMsg) ? ": " + errorMsg : "");
    }
}
