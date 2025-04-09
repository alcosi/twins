package org.twins.core.featurer.pointer;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;

import java.util.HashMap;
import java.util.Properties;

@FeaturerType(id = FeaturerTwins.TYPE_31,
        name = "Pointer",
        description = "Point from given twin to some other twin (linked, head or some other logic)")
@Slf4j
public abstract class Pointer extends FeaturerTwins {
    public TwinEntity point(HashMap<String, String> linkerParams, TwinEntity srcTwinEntity) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, linkerParams, new HashMap<>());
        log.info("Running featurer[{}].point with params: {}", this.getClass().getSimpleName(), properties.toString());
        TwinEntity pointedTwin = point(properties, srcTwinEntity);
        if (pointedTwin == null)
            throw new ServiceException(ErrorCodeTwins.POINTER_ON_NULL);
        return pointedTwin;
    }

    protected abstract TwinEntity point(Properties properties, TwinEntity srcTwinEntity) throws ServiceException;
}
