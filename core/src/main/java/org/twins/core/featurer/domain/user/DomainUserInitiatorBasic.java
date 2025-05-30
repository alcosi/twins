package org.twins.core.featurer.domain.user;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.springframework.stereotype.Component;
import org.twins.core.dao.domain.DomainUserEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsUserGroupId;
import org.twins.core.service.user.UserGroupService;

import java.util.Properties;
import java.util.Set;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_3401,
        name = "Default",
        description = "")
@RequiredArgsConstructor
public class DomainUserInitiatorBasic extends DomainUserInitiator {
    private final UserGroupService userGroupService;

    @FeaturerParam(name = "Enter groups", description = "", order = 1, optional = true)
    public static final FeaturerParamUUIDSet userGroupIds = new FeaturerParamUUIDSetTwinsUserGroupId("enterUserGroups");

    @Override
    protected void init(Properties properties, DomainUserEntity domainUserEntity) throws ServiceException {
        Set<UUID> groupIds = userGroupIds.extract(properties);
        if (groupIds.isEmpty()) {
            return;
        }
        userGroupService.enterGroups(userGroupIds.extract(properties));
    }
}
