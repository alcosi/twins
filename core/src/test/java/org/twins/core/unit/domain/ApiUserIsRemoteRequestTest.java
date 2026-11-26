package org.twins.core.unit.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.twins.core.domain.ApiUser;
import org.twins.core.service.HttpRequestService;
import org.twins.core.service.auth.ApiUserResolverService;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * {@code ApiUser#isRemoteRequest()}: distinguishes request-serving threads from scheduler/async
 * background threads, resolved lazily through {@link HttpRequestService} and cached.
 */
@ExtendWith(MockitoExtension.class)
class ApiUserIsRemoteRequestTest {

    @Mock
    ApiUserResolverService apiUserResolverService;
    @Mock
    HttpRequestService httpRequestService;

    @Test
    void requestBound_trueAndCached() {
        when(apiUserResolverService.getHttpRequestService()).thenReturn(httpRequestService);
        when(httpRequestService.isRequestBound()).thenReturn(true);
        ApiUser apiUser = new ApiUser(apiUserResolverService);
        assertTrue(apiUser.isRemoteRequest());
        assertTrue(apiUser.isRemoteRequest());
        verify(httpRequestService, times(1)).isRequestBound();
    }

    @Test
    void backgroundThread_false() {
        when(apiUserResolverService.getHttpRequestService()).thenReturn(httpRequestService);
        when(httpRequestService.isRequestBound()).thenReturn(false);
        assertFalse(new ApiUser(apiUserResolverService).isRemoteRequest());
    }
}
