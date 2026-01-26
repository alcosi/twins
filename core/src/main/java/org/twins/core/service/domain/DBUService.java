package org.twins.core.service.domain;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.apiuser.DBUMembershipCheck;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.SystemEntityService;
import org.twins.core.service.auth.AuthService;

import java.util.UUID;

@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class DBUService {
    private final AuthService authService;

    public DBUMembershipCheck detectSystemTwinsDBUMembershipCheck(UUID twinClassId) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        DBUMembershipCheck detectedCheck = DBUMembershipCheck.BLOCKED;
        if (SystemEntityService.isTwinClassForUser(twinClassId)) {
            switch (apiUser.getDomain().getDomainType()) {
                case basic:
                    detectedCheck = DBUMembershipCheck.DU;
                    break;
                case b2b:
                    if (false) {//todo for future logic, if current user - is domain manager user, and he is in VIEW_ALL_BUSINESS_ACCOUNTS mode
                        detectedCheck = DBUMembershipCheck.DU;
                    } else {
                        detectedCheck = DBUMembershipCheck.DBU_FOR_USER;
                    }
                    break;
                default:
                    throw new ServiceException(ErrorCodeTwins.DOMAIN_TYPE_UNSUPPORTED);
            }
        } else if (SystemEntityService.isTwinClassForBusinessAccount(twinClassId)) {
            switch (apiUser.getDomain().getDomainType()) {
                case basic:
                    throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_INCORRECT, "search by business account is not suitable for basic domain");
                case b2b:
                    if (false) {//todo for future logic, if current user - is domain manager user, and he is in VIEW_ALL_BUSINESS_ACCOUNTS mode
                        detectedCheck = DBUMembershipCheck.DB;
                    } else {
                        detectedCheck = DBUMembershipCheck.DBU_FOR_BUSINESS_ACCOUNT;
                    }
                    break;
                default:
                    throw new ServiceException(ErrorCodeTwins.DOMAIN_TYPE_UNSUPPORTED);
            }
        }
        return detectedCheck;
    }

    public DBUMembershipCheck detectSystemTwinsDBUMembershipCheck() throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        if (apiUser.isUserSpecified() && apiUser.isDomainSpecified() && apiUser.isBusinessAccountSpecified()) {
            return DBUMembershipCheck.DBU_FOR_USER;
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
