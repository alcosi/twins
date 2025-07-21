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
    public abstract Map<UUID, S> getStatistic(Set<UUID> forTwinIdSet, HashMap<String, String> statisterId) throws ServiceException;
} 