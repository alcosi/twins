package org.twins.core.featurer.twin.sorter;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerType;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.featurer.FeaturerTwins;

import java.util.HashMap;
import java.util.Properties;
import java.util.function.Function;

@FeaturerType(id = FeaturerTwins.TYPE_41,
        name = "Twin Search Sorter",
        description = "Order twin search")
@Slf4j
public abstract class TwinSorter extends FeaturerTwins {
    public Function<Specification<TwinEntity>, Specification<TwinEntity>> createSort(HashMap<String, String> twinSorterParams) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, twinSorterParams, new HashMap<>());
        log.info("Running featurer[{}].createSort with params: {}", this.getClass().getSimpleName(), properties.toString());
        return createSort(properties);
    }

    public abstract Function<Specification<TwinEntity>, Specification<TwinEntity>> createSort(Properties properties) throws ServiceException;

}
