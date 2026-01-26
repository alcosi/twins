package org.twins.core.featurer.twin.validator;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.ValidationResult;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Properties;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_1610,
        name = "Twin is not null",
        description = "")
public class TwinValidatorNotNull extends TwinValidator {

    @Override
    protected ValidationResult isValid(Properties properties, TwinEntity twinEntity, boolean invert) throws ServiceException {
        return buildResult(
                twinEntity != null,
                invert,
                "given twin is null",
                "given twin is not null");
    }

    @Override
    protected boolean nullable() {
        return true;
    }
}
