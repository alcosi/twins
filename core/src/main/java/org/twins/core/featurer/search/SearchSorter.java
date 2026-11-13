package org.twins.core.featurer.search;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.featurer.FeaturerTwins;

import java.util.HashMap;
import java.util.Properties;
import java.util.function.Function;

/**
 * Base class for preset-search sorter featurers. Returns a function wrapping the
 * filter specification with preset ordering, or {@code null} to keep the default sort.
 */
@Slf4j
public abstract class SearchSorter<E> extends FeaturerTwins {
    public Function<Specification<E>, Specification<E>> createSort(HashMap<String, String> sorterParams) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, sorterParams);
        log.info("Running featurer[{}].createSort with params: {}", this.getClass().getSimpleName(), properties.toString());
        return createSort(properties);
    }

    public abstract Function<Specification<E>, Specification<E>> createSort(Properties properties) throws ServiceException;
}
