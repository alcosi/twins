package org.twins.core.domain.apiuser;

import org.twins.core.domain.ApiUser;

import java.util.UUID;

public class UserResolverNotSpecified implements UserResolver {
    @Override
    public UUID resolveCurrentUserId() {
        return ApiUser.NOT_SPECIFIED;
    }
}
