package org.twins.core.featurer.classfield.projector;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;

import java.util.HashMap;
import java.util.Properties;

@FeaturerType(id = FeaturerTwins.TYPE_44,
        name = "FieldProjector",
        description = "Field projections")
@Slf4j
public abstract class FieldProjector extends FeaturerTwins {
    public FieldValue project(HashMap<String, String> fieldProjectionParams, TwinClassFieldEntity field, TwinEntity entity) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, fieldProjectionParams, new HashMap<>());
        return project(properties, field, entity);
    }

    protected abstract FieldValue project(Properties properties, TwinClassFieldEntity field, TwinEntity entity) throws ServiceException;
}
