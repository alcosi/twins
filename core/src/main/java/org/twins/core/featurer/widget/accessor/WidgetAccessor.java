package org.twins.core.featurer.widget.accessor;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.Featurer;
import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.dao.twinclass.TwinClassEntity;

import java.util.HashMap;
import java.util.Properties;


@FeaturerType(id = 14,
        name = "WidgetAccessor",
        description = "Checks if widget is suitable for class")
@Slf4j
public abstract class WidgetAccessor extends Featurer {
    public boolean isAvailableForClass(HashMap<String, String> fieldTyperParams, TwinClassEntity twinClassEntity) throws ServiceException {
        Properties accessorProperties = featurerService.extractProperties(this, fieldTyperParams, new HashMap<>());
        return isAvailableForClass(accessorProperties, twinClassEntity);
    }

    protected abstract boolean isAvailableForClass(Properties properties, TwinClassEntity twinClassEntity);
}
