package org.twins.core.service.twin;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.domain.ApiUser;
import org.twins.core.featurer.fieldtyper.PermissionContext;
import org.twins.core.service.auth.AuthService;

@Component
@RequiredArgsConstructor
public class TwinFieldCalcPermissionContextProvider {
    private final AuthService authService;

    public PermissionContext get() throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        return new PermissionContext(
                apiUser.getUserId(),
                apiUser.getUser().getUserGroupsFootprint()
        );
    }
}
