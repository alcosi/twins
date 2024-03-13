package org.twins.core.domain.apiuser;

import org.twins.core.domain.ApiUser;

import java.util.UUID;

public class DomainResolverNotSpecified implements DomainResolver {

    @Override
    public UUID resolveCurrentDomainId() {
        return ApiUser.NOT_SPECIFIED;
    }
}
