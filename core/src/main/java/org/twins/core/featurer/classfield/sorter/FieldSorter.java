package org.twins.core.featurer.classfield.sorter;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerType;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.featurer.FeaturerTwins;

import java.util.HashMap;
import java.util.Properties;
import java.util.function.Function;

@FeaturerType(id = FeaturerTwins.TYPE_40,
        name = "FieldSorter",
        description = "Order class fields search")
@Slf4j
public abstract class FieldSorter extends FeaturerTwins {
    public Function<Specification<TwinClassFieldEntity>, Specification<TwinClassFieldEntity>> createSort(HashMap<String, String> fieldSorterParams) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, fieldSorterParams);
        log.info("Running featurer[{}].createSort with params: {}", this.getClass().getSimpleName(), properties.toString());
        return createSort(properties);
    }

    public abstract Function<Specification<TwinClassFieldEntity>, Specification<TwinClassFieldEntity>> createSort(Properties properties) throws ServiceException;

}
