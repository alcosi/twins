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
@Featurer(id = FeaturerTwins.ID_1602,
        name = "Twin created by current user",
        description = "")
public class TwinValidatorTwinCreatedByCurrentUser extends TwinValidator {
    @Lazy
    @Autowired
    AuthService authService;
    @Override
    protected ValidationResult isValid(Properties properties, TwinEntity twinEntity, boolean invert) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        boolean isValid = twinEntity.getCreatedByUserId() != null && twinEntity.getCreatedByUserId().equals(apiUser.getUser().getId());
        return buildResult(
                isValid,
                invert,
                twinEntity.logShort() + " is not created by current user[" + apiUser.getUser().getId() + "]",
                twinEntity.logShort() + " is created by current user[" + apiUser.getUser().getId() + "]");
    }
}
