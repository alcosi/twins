package org.twins.core.featurer.classfield.projector;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.featurer.FeaturerTwins;

import java.util.HashMap;
import java.util.Properties;

@FeaturerType(id = FeaturerTwins.TYPE_44,
        name = "FieldProjector",
        description = "Field projections")
@Slf4j
public abstract class FieldProjector extends FeaturerTwins {

    //todo refactor
    public void project(HashMap<String, String> fieldProjectionParams) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, fieldProjectionParams, new HashMap<>());
        project(properties);
    }

    protected abstract void project(Properties properties) throws ServiceException;
}
