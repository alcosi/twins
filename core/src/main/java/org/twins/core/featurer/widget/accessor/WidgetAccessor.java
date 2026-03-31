package org.twins.core.featurer.widget.accessor;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.featurer.FeaturerTwins;

import java.util.HashMap;
import java.util.Properties;


@FeaturerType(id = FeaturerTwins.TYPE_14,
        name = "WidgetAccessor",
        description = "Checks if widget is suitable for class")
@Slf4j
public abstract class WidgetAccessor extends FeaturerTwins {
    public boolean isAvailableForClass(HashMap<String, String> fieldTyperParams, TwinClassEntity twinClassEntity) throws ServiceException {
        Properties accessorProperties = featurerService.extractProperties(this, fieldTyperParams);
        return isAvailableForClass(accessorProperties, twinClassEntity);
    }

    protected abstract boolean isAvailableForClass(Properties properties, TwinClassEntity twinClassEntity);
}
