package org.twins.core.unit.service;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.twins.core.service.HttpRequestService;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@code HttpRequestService#isAdminArea()}: parses the AdminArea header only when a request is
 * bound to the current thread; safely returns false in scheduler/async threads (no request proxy
 * to resolve).
 */
class HttpRequestServiceAdminAreaTest {

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void noRequestBound_returnsFalse() {
        HttpRequestService service = new HttpRequestService(mock(HttpServletRequest.class));
        assertFalse(service.isAdminArea());
    }

    @Test
    void headerTrue_returnsTrue() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(HttpRequestService.HEADER_ADMIN_AREA)).thenReturn("true");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
        assertTrue(new HttpRequestService(request).isAdminArea());
    }

    @Test
    void headerAbsent_returnsFalse() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
        assertFalse(new HttpRequestService(request).isAdminArea());
    }

    @Test
    void headerGarbage_returnsFalse() {
        // only the literal "true" counts — anything else must not enable the bypass
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(HttpRequestService.HEADER_ADMIN_AREA)).thenReturn("yes");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
        assertFalse(new HttpRequestService(request).isAdminArea());
    }

    @Test
    void isRequestBound_noRequest_returnsFalse() {
        assertFalse(new HttpRequestService(mock(HttpServletRequest.class)).isRequestBound());
    }

    @Test
    void isRequestBound_requestBound_returnsTrue() {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
        assertTrue(new HttpRequestService(mock(HttpServletRequest.class)).isRequestBound());
    }
}
