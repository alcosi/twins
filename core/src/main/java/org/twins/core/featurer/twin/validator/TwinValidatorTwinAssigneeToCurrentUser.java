package org.twins.core.featurer.twin.validator;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.ValidationResult;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.service.auth.AuthService;

import java.util.Properties;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_1601,
        name = "Twin assignee to current user",
        description = "")
public class TwinValidatorTwinAssigneeToCurrentUser extends TwinValidator {
    @Lazy
    @Autowired
    AuthService authService;
    @Override
    protected ValidationResult isValid(Properties properties, TwinEntity twinEntity, boolean invert) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        boolean isValid = twinEntity.getAssignerUserId() != null && twinEntity.getAssignerUserId().equals(apiUser.getUser().getId());
        return buildResult(
                isValid,
                invert,
                twinEntity.logShort() + " is not assignee to current user[" + apiUser.getUser().getId() + "]",
                twinEntity.logShort() + " is assignee to current user[" + apiUser.getUser().getId() + "]");
    }
}
