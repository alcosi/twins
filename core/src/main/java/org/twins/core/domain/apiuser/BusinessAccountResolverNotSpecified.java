package org.twins.core.domain.apiuser;

import org.twins.core.domain.ApiUser;

import java.util.UUID;

public class BusinessAccountResolverNotSpecified implements BusinessAccountResolver {
    @Override
    public UUID resolveCurrentBusinessAccountId() {
        return ApiUser.NOT_SPECIFIED;
    }
}
