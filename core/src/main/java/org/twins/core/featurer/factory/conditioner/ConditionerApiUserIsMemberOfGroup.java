package org.twins.core.featurer.factory.conditioner;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsUserGroupId;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.user.UserGroupService;

import java.util.Properties;
import java.util.Set;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_2423,
        name = "Is member of group",
        description = "")
@Slf4j
public class ConditionerApiUserIsMemberOfGroup extends Conditioner {

    @Lazy
    @Autowired
    AuthService authService;

    @Autowired
    private UserGroupService userGroupService;

    @FeaturerParam(name = "User group ids", description = "", order = 1)
    public static final FeaturerParamUUIDSet userGroupIds = new FeaturerParamUUIDSetTwinsUserGroupId("userGroupIds");

    @Override
    public boolean check(Properties properties, FactoryItem factoryItem) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        userGroupService.loadGroups(apiUser);
        Set<UUID> propertiesUuids = userGroupIds.extract(properties);
        return apiUser.getUserGroups() != null && apiUser.getUserGroups().stream().anyMatch(propertiesUuids::contains);
    }
}
