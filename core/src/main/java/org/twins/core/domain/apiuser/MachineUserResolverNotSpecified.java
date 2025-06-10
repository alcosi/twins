package org.twins.core.domain.apiuser;

import org.cambium.common.exception.ServiceException;
import org.twins.core.domain.ApiUser;

import java.util.UUID;

public class MachineUserResolverNotSpecified implements MachineUserResolver {
    public static final MachineUserResolver instance = new MachineUserResolverNotSpecified();

    @Override
    public UUID resolveCurrentMachineUserId() throws ServiceException {
        return ApiUser.NOT_SPECIFIED;
    }
}
