package org.twins.core.service.domain;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.apiuser.DBUMembershipCheck;
import org.twins.core.service.auth.AuthService;

@Service
@RequiredArgsConstructor
public class DBUService {
    private final AuthService authService;

    public DBUMembershipCheck detectSystemTwinsDBUMembershipCheck(TwinClassEntity.OwnerType targetClassOwnerType) throws ServiceException {
        return switch (targetClassOwnerType) {
            case DOMAIN_BUSINESS_ACCOUNT, DOMAIN_BUSINESS_ACCOUNT_USER -> DBUMembershipCheck.DBU;
            case USER, BUSINESS_ACCOUNT -> DBUMembershipCheck.BU;
            case DOMAIN_USER, DOMAIN -> DBUMembershipCheck.DU;
            default -> throw new ServiceException(ErrorCodeCommon.UNEXPECTED_SERVER_EXCEPTION, "unsupported owner type");
        };
    }

    public DBUMembershipCheck detectSystemTwinsDBUMembershipCheck() throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        if (apiUser.isUserSpecified() && apiUser.isDomainSpecified() && apiUser.isBusinessAccountSpecified()) {
            return DBUMembershipCheck.DBU;
        } else if (apiUser.isUserSpecified() && apiUser.isBusinessAccountSpecified()) {
            return DBUMembershipCheck.BU;
        } else if (apiUser.isUserSpecified() && apiUser.isDomainSpecified()) {
            return DBUMembershipCheck.DU;
        } else if (apiUser.isDomainSpecified() && apiUser.isBusinessAccountSpecified()) {
            return DBUMembershipCheck.DB;
        } else {
            throw new ServiceException(ErrorCodeCommon.UNEXPECTED_SERVER_EXCEPTION, "unsupported owner type");
        }
    }
}
