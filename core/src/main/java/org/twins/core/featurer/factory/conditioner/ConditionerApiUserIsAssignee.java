package org.twins.core.featurer.factory.conditioner;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.service.auth.AuthService;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_2404,
        name = "ConditionerApiUserIsAssignee",
        description = "")
@Slf4j
public class ConditionerApiUserIsAssignee extends Conditioner {

    @Lazy
    @Autowired
    AuthService authService;

    @Override
    public boolean check(Properties properties, FactoryItem factoryItem) throws ServiceException {
        return authService.getApiUser().getUserId().equals(factoryItem.getTwin().getAssignerUserId());
    }
}
