package org.twins.core.domain.apiuser;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.service.HttpRequestService;

/**
 * Resolves the admin-area mark from the {@code AdminArea} request header (the FE sets it solely
 * for admin-zone requests). Safe in non-request threads (schedulers, async): resolves to false
 * because no request is bound — see {@link HttpRequestService#isAdminArea()}.
 */
@Component
@RequiredArgsConstructor
public class AdminAreaResolverHeaders implements AdminAreaResolver {
    final HttpRequestService httpRequestService;

    @Override
    public boolean resolveAdminArea() {
        return httpRequestService.isAdminArea();
    }
}
