package org.twins.core.domain.apiuser;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.service.HttpRequestService;

/**
 * Resolves the admin-area mark from the {@code AdminArea} request header (the FE sets it solely
 * for admin-zone requests). Safe in non-request threads (schedulers, async): resolves to false
 * because no request is bound — see {@link HttpRequestService#isAdminArea()}.
 * <p><b>Not a security boundary:</b> the header is client-controlled — any holder of the gated
 * permission can set it. The permission check remains the actual access control.
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
