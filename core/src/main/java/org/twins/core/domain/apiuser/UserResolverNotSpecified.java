package org.twins.core.domain.apiuser;

import org.twins.core.domain.ApiUser;

import java.util.UUID;

public class UserResolverNotSpecified implements UserResolver {
    public static final UserResolver instance = new UserResolverNotSpecified();

    @Override
    public UUID resolveCurrentUserId() {
        return ApiUser.NOT_SPECIFIED;
    }
}
