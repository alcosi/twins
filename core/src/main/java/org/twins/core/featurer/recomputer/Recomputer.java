package org.twins.core.featurer.recomputer;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.service.recompute.FieldRecomputeRequest;

import java.util.HashMap;
import java.util.Properties;

/**
 * Strategy invoked by {@code TwinRecomputeService} for each recompute target (subscriber field).
 * Resolves its {@code @FeaturerParam} params from the subscriber row's {@code recomputer_params} hstore,
 * then delegates to {@link #recompute(FieldRecomputeRequest, TwinChangesCollector, Properties)}.
 * Default implementation {@code RecomputerByFieldTyper} delegates to the subscriber field's FieldTyper.
 */
@FeaturerType(id = FeaturerTwins.TYPE_55,
        name = "Recomputer",
        description = "Strategy invoked by TwinRecomputeService per subscriber field. " +
                "Default impl delegates to the subscriber field's FieldTyper if it implements FieldTyperRecomputed.")
@Slf4j
public abstract class Recomputer extends FeaturerTwins {

    public void recompute(FieldRecomputeRequest request, TwinChangesCollector collector,
                          HashMap<String, String> params) throws ServiceException {
        Properties properties = params == null ? new Properties() : extractProperties(params);
        recompute(request, collector, properties);
    }

    public abstract void recompute(FieldRecomputeRequest request, TwinChangesCollector collector,
                                   Properties properties) throws ServiceException;
}
