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
@Featurer(id = FeaturerTwins.ID_1624,
        name = "Twin update operation",
        description = "Validator for checking that the twin is being updated (not created). Driven by the TwinEntity.createElseUpdate flag: valid when the flag is false (update), invalid when it is true (create). Use the inverted mode to assert a create operation instead.")
public class TwinValidatorUpdateOperation extends TwinValidator {

    @Override
    protected CollectionValidationResult isValid(Properties properties, Collection<TwinEntity> twinEntityCollection, boolean invert) throws ServiceException {
        var result = new CollectionValidationResult();
        for (var twinEntity : twinEntityCollection) {
            // createElseUpdate == true means a create operation; an update operation is when it is false.
            boolean isUpdate = !twinEntity.isCreateElseUpdate();
            result.getTwinsResults().put(twinEntity.getId(), buildResult(
                    isUpdate,
                    invert,
                    twinEntity.logShort() + " is being created, not updated",
                    twinEntity.logShort() + " is being updated, not created"));
        }
        return result;
    }
}
