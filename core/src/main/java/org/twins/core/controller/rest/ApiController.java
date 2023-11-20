package org.twins.core.controller.rest;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ErrorCode;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.i18n.service.I18nService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.twins.core.dao.error.ErrorEntity;
import org.twins.core.dao.error.ErrorRepository;
import org.twins.core.dto.rest.Response;

import java.util.Hashtable;

@Slf4j
public abstract class ApiController {
    @Autowired
    private ErrorRepository errorRepository;

    @Autowired
    private I18nService i18NService;

    protected void logException(Exception ex) {
        if (ex instanceof ServiceException)
            log.error(((ServiceException) ex).log());
        else
            log.error("Exception: ", ex);
    }

    private ResponseEntity<Response> createErrorRs(Exception ex, int statusCode, String defaultMsg, HttpStatus httpStatus, Response rs, Hashtable<String, String> context) {
        rs = rs == null ? new Response() : rs;
        rs.setStatus(statusCode);
        logException(ex);
        ErrorEntity errorEntity = errorRepository.findByErrorCodeLocal(statusCode);
        if (errorEntity != null) {
            rs.setMsg(i18NService.translateToLocale(errorEntity.getClientMsgI18n(), context));
        } else
            rs.setMsg(defaultMsg);

        return new ResponseEntity<>(rs, httpStatus);
    }

    public ResponseEntity<Response> createErrorRs(Exception ex, ErrorCode errorCode, Response rs) {
        if (ex instanceof ServiceException)
            return createErrorRs((ServiceException) ex, rs);
        return createErrorRs(ex, errorCode.getCode(), errorCode.getMessage(), errorCode.getHttpStatus(), rs, null);
    }

    public ResponseEntity<Response> createErrorRs(ServiceException ex, Response rs) {
        return createErrorRs(ex, ex.getErrorCode(), ex.getMessage(), ex.getHttpStatus(), rs, ex.getContext());
    }

    public ResponseEntity<Response> createErrorRs(Exception ex, Response rs) {
        return createErrorRs(ex, ErrorCodeCommon.UNEXPECTED_SERVER_EXCEPTION, rs);
    }

    public ResponseEntity<Response> createErrorRs(Exception ex, ErrorCode errorCode) {
        return createErrorRs(ex, errorCode, new Response());
    }

    public ResponseEntity<Response> createErrorRs(ServiceException ae) {
        return createErrorRs(ae, new Response());
    }

    public ResponseEntity<Response> createErrorRs(Exception ex) {
        return createErrorRs(ex, new Response());
    }
}
