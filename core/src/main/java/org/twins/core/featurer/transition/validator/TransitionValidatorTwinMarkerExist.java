package org.twins.core.featurer.transition.validator;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.service.twin.TwinMarkerService;

import java.util.Properties;

@Slf4j
@Component
@Featurer(id = 1603,
        name = "TransitionValidatorTwinMarkerExist",
        description = "")
public class TransitionValidatorTwinMarkerExist extends TransitionValidator {
    @FeaturerParam(name = "markerId", description = "")
    public static final FeaturerParamUUID markerId = new FeaturerParamUUID("markerId");
    @Lazy
    @Autowired
    TwinMarkerService twinMarkerService;
    @Override
    protected ValidationResult isValid(Properties properties, TwinEntity twinEntity) throws ServiceException {
        boolean isValid = twinMarkerService.hasMarker(twinEntity, markerId.extract(properties));
        return new ValidationResult()
                .setValid(isValid)
                .setMessage(isValid ? "" : twinEntity.logShort() + " does not have marker[" + markerId.extract(properties) + "]");
    }
}
