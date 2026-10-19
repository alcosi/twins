package org.twins.core.config.advice;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ErrorCode;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.twins.core.dao.error.ErrorEntity;
import org.twins.core.dao.error.ErrorRepository;
import org.twins.core.dto.rest.Response;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.i18n.I18nService;

import java.util.stream.Collectors;

/**
 * Converts framework-level request validation failures into the project error envelope
 * {status, msg, statusDetails} (see ApiController#createErrorRs). Without this advice such
 * failures bypass the controllers' try/catch (they are thrown before the method body starts)
 * and clients would receive the default Spring Boot error body instead.
 */
@Slf4j
@RestControllerAdvice
public class ValidationExceptionHandlingAdvice {

    private final ErrorRepository errorRepository;
    private final I18nService i18nService;

    public ValidationExceptionHandlingAdvice(ErrorRepository errorRepository, I18nService i18nService) {
        this.errorRepository = errorRepository;
        this.i18nService = i18nService;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        String globalErrors = ex.getBindingResult().getGlobalErrors().stream()
                .map(oe -> oe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        if (!globalErrors.isEmpty())
            details = details.isEmpty() ? globalErrors : details + "; " + globalErrors;
        return createErrorRs(ErrorCodeTwins.VALIDATION_DTO_FAILED, details, ex);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<Response> handleHandlerMethodValidation(HandlerMethodValidationException ex) {
        String details = ex.getAllErrors().stream()
                .map(MessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return createErrorRs(ErrorCodeTwins.VALIDATION_DTO_FAILED, details, ex);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Response> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        String details = ex.getMessage() != null ? ex.getMessage() : "request body is not readable";
        return createErrorRs(ErrorCodeTwins.MALFORMED_REQUEST_BODY, details, ex);
    }

    private ResponseEntity<Response> createErrorRs(ErrorCode errorCode, String statusDetails, Exception ex) {
        Response rs = new Response();
        rs.setStatus(errorCode.getCode());
        rs.setStatusDetails(statusDetails);
        log.error("Exception: ", ex);
        ErrorEntity errorEntity = errorRepository.findByErrorCodeLocal(errorCode.getCode());
        if (errorEntity != null)
            rs.setMsg(i18nService.translateToLocale(errorEntity.getClientMsgI18nId()));
        else
            rs.setMsg("error");
        return new ResponseEntity<>(rs, errorCode.getHttpStatus());
    }
}
