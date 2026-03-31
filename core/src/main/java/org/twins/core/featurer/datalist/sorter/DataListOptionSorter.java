package org.twins.core.featurer.datalist.sorter;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerType;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.featurer.FeaturerTwins;

import java.util.HashMap;
import java.util.Properties;
import java.util.function.Function;

@FeaturerType(id = FeaturerTwins.TYPE_52,
        name = "DataListOptionSorter",
        description = "Order data list option search")
@Slf4j
public abstract class DataListOptionSorter extends FeaturerTwins {
    public Function<Specification<DataListOptionEntity>, Specification<DataListOptionEntity>> createSort(HashMap<String, String> optionSorterParams) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, optionSorterParams);
        log.info("Running featurer[{}].createSort with params: {}", this.getClass().getSimpleName(), properties.toString());
        return createSort(properties);
    }

    public abstract Function<Specification<DataListOptionEntity>, Specification<DataListOptionEntity>> createSort(Properties properties) throws ServiceException;
}
