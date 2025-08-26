package org.twins.core.featurer.twin.validator;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamString;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Properties;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_1616,
        name = "Twin external id",
        description = "")
public class TwinValidatorExternalId extends TwinValidator {
    @FeaturerParam(name = "External Id", description = "", optional = true, order = 1)
    public static final FeaturerParamString externalId = new FeaturerParamString("externalId");

    @Override
    protected ValidationResult isValid(Properties properties, TwinEntity twinEntity, boolean invert) throws ServiceException {
        String expectedExternalId = externalId.extract(properties);
        String twinExternalId = twinEntity.getExternalId();

        boolean isValid;

        if (expectedExternalId == null) {
            isValid = twinExternalId == null;
        } else {
            isValid = expectedExternalId.equals(twinExternalId);
        }

        return buildResult(
                isValid,
                invert,
                twinEntity.logShort() + " external ids doesn't match",
                twinEntity.logShort() + " external ids matches");
    }
}
