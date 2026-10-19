package org.twins.core.config.advice;

import org.cambium.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.twins.core.dao.error.ErrorEntity;
import org.twins.core.dao.error.ErrorRepository;
import org.twins.core.dto.rest.Response;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.i18n.I18nService;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ValidationExceptionHandlingAdviceTest {

    @Mock
    private ErrorRepository errorRepository;

    @Mock
    private I18nService i18nService;

    @InjectMocks
    private ValidationExceptionHandlingAdvice advice;

    @Test
    public void testMethodArgumentNotValid_ProjectEnvelope() throws NoSuchMethodException {
        ErrorCode errorCode = ErrorCodeTwins.VALIDATION_DTO_FAILED;
        ErrorEntity errorEntity = new ErrorEntity();
        errorEntity.errorCodeLocal = errorCode.getCode();
        errorEntity.setClientMsgI18nId(UUID.fromString("00000000-0000-0000-0012-0000000011d2"));
        when(errorRepository.findByErrorCodeLocal(errorCode.getCode())).thenReturn(errorEntity);
        when(i18nService.translateToLocale(errorEntity.getClientMsgI18nId())).thenReturn("Some of the request fields are invalid");

        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "storageLink", "must not be blank"));
        bindingResult.addError(new FieldError("request", "twinId", "must not be null"));
        MethodParameter parameter = new MethodParameter(
                ValidationExceptionHandlingAdviceTest.class.getDeclaredMethod("testMethodArgumentNotValid_ProjectEnvelope"), -1);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);

        ResponseEntity<Response> responseEntity = advice.handleMethodArgumentNotValid(ex);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        Response rs = responseEntity.getBody();
        assertNotNull(rs);
        assertEquals(ErrorCodeTwins.VALIDATION_DTO_FAILED.getCode(), rs.getStatus());
        assertEquals("Some of the request fields are invalid", rs.getMsg());
        assertTrue(rs.getStatusDetails().contains("storageLink: must not be blank"));
        assertTrue(rs.getStatusDetails().contains("twinId: must not be null"));
    }

    @Test
    public void testErrorEntityMissing_FallbackMsg() throws NoSuchMethodException {
        when(errorRepository.findByErrorCodeLocal(anyInt())).thenReturn(null);

        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "key", "must not be blank"));
        MethodParameter parameter = new MethodParameter(
                ValidationExceptionHandlingAdviceTest.class.getDeclaredMethod("testErrorEntityMissing_FallbackMsg"), -1);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);

        ResponseEntity<Response> responseEntity = advice.handleMethodArgumentNotValid(ex);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        Response rs = responseEntity.getBody();
        assertNotNull(rs);
        assertEquals(ErrorCodeTwins.VALIDATION_DTO_FAILED.getCode(), rs.getStatus());
        assertEquals("error", rs.getMsg());
        assertTrue(rs.getStatusDetails().contains("key: must not be blank"));
        // i18n must not be touched when the error row is absent
        org.mockito.Mockito.verify(i18nService, org.mockito.Mockito.never()).translateToLocale(any(UUID.class));
    }

    @Test
    public void testHttpMessageNotReadable_MalformedBodyCode() {
        ErrorEntity errorEntity = new ErrorEntity();
        errorEntity.errorCodeLocal = ErrorCodeTwins.MALFORMED_REQUEST_BODY.getCode();
        errorEntity.setClientMsgI18nId(UUID.fromString("00000000-0000-0000-0012-0000000011d3"));
        when(errorRepository.findByErrorCodeLocal(ErrorCodeTwins.MALFORMED_REQUEST_BODY.getCode())).thenReturn(errorEntity);
        when(i18nService.translateToLocale(errorEntity.getClientMsgI18nId())).thenReturn("Request body is malformed or unreadable");

        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("JSON parse error: Unexpected character",
                (org.springframework.http.HttpInputMessage) null);

        ResponseEntity<Response> responseEntity = advice.handleHttpMessageNotReadable(ex);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        Response rs = responseEntity.getBody();
        assertNotNull(rs);
        assertEquals(ErrorCodeTwins.MALFORMED_REQUEST_BODY.getCode(), rs.getStatus());
        assertEquals("Request body is malformed or unreadable", rs.getMsg());
        assertTrue(rs.getStatusDetails().contains("JSON parse error"));
    }
}
