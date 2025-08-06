package org.twins.core.featurer.classfield.sorter;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerType;
import org.springframework.data.domain.Sort;
import org.twins.core.featurer.FeaturerTwins;

import java.util.HashMap;
import java.util.Properties;

@FeaturerType(id = FeaturerTwins.TYPE_40,
        name = "FieldSorter",
        description = "Order class fields search")
@Slf4j
public abstract class FieldSorter extends FeaturerTwins {
    public Sort createSort(HashMap<String, String> fieldSorterParams) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, fieldSorterParams, new HashMap<>());
        log.info("Running featurer[{}].createSort with params: {}", this.getClass().getSimpleName(), properties.toString());
        return createSort(properties);
    }

    public abstract Sort createSort(Properties properties) throws ServiceException;

}
