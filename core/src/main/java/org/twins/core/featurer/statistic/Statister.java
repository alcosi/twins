package org.twins.core.featurer.statistic;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.domain.TwinStatistic;
import org.twins.core.featurer.FeaturerTwins;

import java.util.*;

@FeaturerType(id = FeaturerTwins.TYPE_38,
        name = "Statister",
        description = "Get statistic")
@Slf4j
public abstract class Statister<S extends TwinStatistic> extends FeaturerTwins {
    public Map<UUID, S> getStatistic(HashMap<String, String> statisterParams, Set<UUID> forTwinIdSet) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, statisterParams);
        log.info("Running featurer[{}].statister with params: {}", this.getClass().getSimpleName(), properties.toString());
        return getStatistic(properties, forTwinIdSet);
    }

    public abstract Map<UUID, S> getStatistic(Properties properties, Set<UUID> forTwinIdSet) throws ServiceException;
} 