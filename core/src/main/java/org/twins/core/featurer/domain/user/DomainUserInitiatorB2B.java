package org.twins.core.featurer.domain.user;

import com.github.f4b6a3.uuid.UuidCreator;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.cambium.service.EntitySmartService;
import org.springframework.stereotype.Component;
import org.twins.core.dao.businessaccount.BusinessAccountEntity;
import org.twins.core.dao.domain.DomainUserEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsUserGroupId;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.businessaccount.BusinessAccountService;
import org.twins.core.service.businessaccount.BusinessAccountUserService;
import org.twins.core.service.domain.DomainBusinessAccountService;
import org.twins.core.service.user.UserGroupService;

import java.util.Properties;
import java.util.Set;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_3402,
        name = "B2B Main",
        description = "")
@RequiredArgsConstructor
public class DomainUserInitiatorB2B extends DomainUserInitiator {
    private final UserGroupService userGroupService;
    private final DomainBusinessAccountService domainBusinessAccountService;
    private final AuthService authService;

    @FeaturerParam(name = "Enter groups", description = "", order = 1, optional = true)
    public static final FeaturerParamUUIDSet userGroupIds = new FeaturerParamUUIDSetTwinsUserGroupId("enterUserGroups");

    @FeaturerParam(name = "Auto create business account", description = "", order = 1, optional = true)
    public static final FeaturerParamBoolean autoCreateBusinessAccount = new FeaturerParamBoolean("autoCreateBusinessAccount");
    private final BusinessAccountUserService businessAccountUserService;
    private final BusinessAccountService businessAccountService;

    @Override
    protected void init(Properties properties, DomainUserEntity domainUserEntity) throws ServiceException {
    }

    @Override
    protected void postInit(Properties properties, DomainUserEntity domainUserEntity) throws ServiceException {
        super.postInit(properties, domainUserEntity);
        if (autoCreateBusinessAccount.extract(properties)) {
            //perhaps we need to grant for current user some permissions (DOMAIN_BUSINESS_ACCOUNT_CREATE))
            UUID newBusinessAccountId = UuidCreator.getTimeOrderedEpoch();
            BusinessAccountEntity businessAccount = businessAccountService
                    .addBusinessAccount(newBusinessAccountId, "New company", EntitySmartService.SaveMode.ifPresentThrowsElseCreate);
            domainBusinessAccountService.addBusinessAccount(businessAccount, null, false);
            businessAccountUserService.addUser(newBusinessAccountId, authService.getApiUser().getUserId(), false);
            domainUserEntity.setLastActiveBusinessAccountId(newBusinessAccountId);
        }
        Set<UUID> groupIds = userGroupIds.extract(properties);
        if (groupIds.isEmpty()) {
            return;
        }
        // we can not switch active business account here, because user is activated, but not logged in (no token from IDP)
        // identityProviderService.switchActiveBusinessAccount(newBusinessAccountId);
        userGroupService.manageForUser(domainUserEntity.getUserId(), userGroupIds.extract(properties), null);
    }
}
