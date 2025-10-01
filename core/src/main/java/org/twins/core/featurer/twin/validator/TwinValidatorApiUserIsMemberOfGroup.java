package org.twins.core.featurer.twin.validator;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.ValidationResult;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.KitUtils;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsUserGroupId;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.user.UserGroupService;

import java.util.Properties;
import java.util.Set;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_1607,
        name = "Current user is member of group",
        description = "")
@Slf4j
public class TwinValidatorApiUserIsMemberOfGroup extends TwinValidator {

    @Lazy
    @Autowired
    AuthService authService;

    @Autowired
    private UserGroupService userGroupService;

    @FeaturerParam(name = "User group ids", description = "", order = 1)
    public static final FeaturerParamUUIDSet userGroupIds = new FeaturerParamUUIDSetTwinsUserGroupId("userGroupIds");

    @Override
    protected ValidationResult isValid(Properties properties, TwinEntity twinEntity, boolean invert) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        userGroupService.loadGroupsForCurrentUser();
        Set<UUID> propertiesUuids = userGroupIds.extract(properties);
        boolean isValid = KitUtils.isNotEmpty(apiUser.getUser().getUserGroups()) && apiUser.getUser().getUserGroups().getIdSet().stream().anyMatch(propertiesUuids::contains);
        return buildResult(
                isValid,
                invert,
                "User[" + apiUser.getUser().getId() + "," + apiUser.getBusinessAccountId() + "] is not a member of any of these groups.",
                "User[" + apiUser.getUser().getId() + "," + apiUser.getBusinessAccountId() + "] is a member of any of these groups.");
    }
}
