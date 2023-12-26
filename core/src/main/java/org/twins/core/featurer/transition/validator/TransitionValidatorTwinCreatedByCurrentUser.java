package org.twins.core.featurer.transition.validator;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.service.auth.AuthService;

import java.util.Properties;

@Slf4j
@Component
@Featurer(id = 1602,
        name = "TransitionValidatorTwinCreatedByCurrentUser",
        description = "")
public class TransitionValidatorTwinCreatedByCurrentUser extends TransitionValidator {
    @Lazy
    @Autowired
    AuthService authService;
    @Override
    protected boolean isValid(Properties properties, TwinEntity twinEntity) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        return twinEntity.getCreatedByUserId() != null && twinEntity.getCreatedByUserId().equals(apiUser.getUser().getId());
    }
}
