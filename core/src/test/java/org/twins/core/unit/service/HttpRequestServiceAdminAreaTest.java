package org.twins.core.unit.service;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.twins.core.service.HttpRequestService;
import org.twins.core.service.auth.ActAsUserService;

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
        HttpRequestService service = new HttpRequestService(new ActAsUserService(), mock(HttpServletRequest.class));
        assertFalse(service.isAdminArea());
    }

    @Test
    void headerTrue_returnsTrue() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(HttpRequestService.HEADER_ADMIN_AREA)).thenReturn("true");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
        assertTrue(new HttpRequestService(new ActAsUserService(), request).isAdminArea());
    }

    @Test
    void headerAbsent_returnsFalse() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
        assertFalse(new HttpRequestService(new ActAsUserService(), request).isAdminArea());
    }
}
