package org.twins.core.featurer.twin.validator;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Collection;
import java.util.Properties;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_1618,
        name = "Twin field assignee is null",
        description = "")
public class TwinValidatorTwinAssigneeIsNull extends TwinValidator {
    @Override
    protected CollectionValidationResult isValid(Properties properties, Collection<TwinEntity> twinEntityCollection, boolean invert) throws ServiceException {
        CollectionValidationResult result = new CollectionValidationResult();
        for (var twinEntity : twinEntityCollection) {
            boolean isValid = twinEntity.getAssignerUserId() == null;
            result.getTwinsResults().put(twinEntity.getId(), buildResult(
                    isValid,
                    invert,
                    twinEntity.logShort() + " contains assignee",
                    twinEntity.logShort() + " not contains assignee"));
        }
        return result;
    }
}
