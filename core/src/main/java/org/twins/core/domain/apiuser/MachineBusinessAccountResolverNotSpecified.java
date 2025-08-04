package org.twins.core.domain.apiuser;

import org.cambium.common.exception.ServiceException;
import org.twins.core.domain.ApiUser;

import java.util.UUID;

public class MachineBusinessAccountResolverNotSpecified implements MachineBusinessAccountResolver {
    public static final MachineBusinessAccountResolver instance = new MachineBusinessAccountResolverNotSpecified();

    @Override
    public UUID resolveMachineBusinessAccountId() throws ServiceException {
        return ApiUser.NOT_SPECIFIED;
    }
}
