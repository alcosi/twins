package org.twins.core.featurer.recomputer;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.FieldTyperRecomputed;
import org.twins.core.service.recompute.FieldRecomputeRequest;

import java.util.Properties;

/**
 * Default {@link Recomputer}: resolves the subscriber field's FieldTyper and, if it implements
 * {@link FieldTyperRecomputed}, calls {@code recompute}. Otherwise logs and skips. This is the exact
 * behavior of the pre-TWINS-893 {@code TwinFieldRecomputeService} dispatch path.
 */
@Component
@Featurer(id = FeaturerTwins.ID_5501,
        name = "Recomputer by FieldTyper",
        description = "Resolves the subscriber field's FieldTyper; if it implements FieldTyperRecomputed, " +
                "calls recompute. Else logs and skips. Preserves pre-TWINS-893 behavior.")
@Slf4j
public class RecomputerByFieldTyper extends Recomputer {

    @Override
    public void recompute(FieldRecomputeRequest request, TwinChangesCollector collector,
                          Properties properties) throws ServiceException {
        TwinClassFieldEntity subscriberField = request.subscriberField();
        FieldTyper fieldTyper = featurerService.getFeaturer(subscriberField.getFieldTyperFeaturerId(), FieldTyper.class);
        if (!(fieldTyper instanceof FieldTyperRecomputed subscriber)) {
            log.warn("FieldTyper {} for field {} is not a FieldTyperRecomputed, skipping",
                    fieldTyper.getClass().getSimpleName(), subscriberField.getId());
            return;
        }
        subscriber.recompute(request, collector);
    }
}
