package org.twins.core.featurer.twin.validator;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.ValidationResult;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.space.SpaceRoleUserEntity;
import org.twins.core.dao.space.SpaceRoleUserRepository;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsMarkerId;
import org.twins.core.service.auth.AuthService;

import java.util.List;
import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_1612,
        name = "Current user is member of space",
        description = "")
@Slf4j
public class TwinValidatorApiUserIsMemberOfSpace extends TwinValidator {

    @FeaturerParam(name = "Space role id", description = "", order = 1)
    public static final FeaturerParamUUID spaceRoleId = new FeaturerParamUUIDTwinsMarkerId("spaceRoleId");
    @Lazy
    @Autowired
    AuthService authService;
    @Autowired
    private SpaceRoleUserRepository spaceRoleUserRepository;

    @Override
    protected ValidationResult isValid(Properties properties, TwinEntity twinEntity, boolean invert) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        UUID spaceRole = spaceRoleId.extract(properties);
        List<SpaceRoleUserEntity> result = spaceRoleUserRepository.findAllByTwinIdAndSpaceRoleIdAndUserId(twinEntity.getId(), spaceRole, apiUser.getUserId());
        boolean isValid = CollectionUtils.isNotEmpty(result) && result.size() == 1;
        return buildResult(
                isValid,
                invert,
                "User[" + apiUser.getUser().getId() + "," + apiUser.getBusinessAccountId() + "] is not a member of space[" + twinEntity.getId() + "]",
                "User[" + apiUser.getUser().getId() + "," + apiUser.getBusinessAccountId() + "] is a member of space[" + twinEntity.getId() + "]");
    }
}
