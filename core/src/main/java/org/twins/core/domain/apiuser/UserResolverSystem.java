package org.twins.core.domain.apiuser;

import org.twins.core.service.user.UserService;

public class UserResolverSystem extends UserResolverGivenId {
    static UserResolverSystem instance = new UserResolverSystem();

    public static UserResolverSystem getInstance() {
        return instance;
    }
    public UserResolverSystem() {
        super(UserService.SYSTEM_USER_ID);
    }
}
