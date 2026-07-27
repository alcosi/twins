package org.twins.core.featurer.recomputer;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinStatusId;
import org.twins.core.service.history.HistoryService;
import org.twins.core.service.recompute.FieldRecomputeRequest;
import org.twins.core.service.twin.TwinStatusService;

import java.util.Properties;

/**
 * {@link Recomputer} that flips the subscriber twin's status to the one declared in params and records a
 * {@code statusChanged} history entry. Mirrors {@code TwinService.changeStatus}, but writes into the
 * collector passed by the dispatcher (persistence is driven by the caller's {@code applyChanges}), so no
 * separate transaction / collector is created here.
 */
@Component
@Featurer(id = FeaturerTwins.ID_5502,
        name = "Recomputer Change Status",
        description = "Sets the subscriber twin's status to the target status from params and records a statusChanged history entry.")
@Slf4j
public class RecomputerChangeStatus extends Recomputer {

    @FeaturerParam(name = "Target status id", description = "Twin status to set on the subscriber twin", order = 1)
    public static final FeaturerParamUUIDTwinsTwinStatusId targetStatusId = new FeaturerParamUUIDTwinsTwinStatusId("targetStatusId");

    @Lazy
    @Autowired
    private TwinStatusService twinStatusService;
    @Lazy
    @Autowired
    private HistoryService historyService;

    @Override
    public void recompute(FieldRecomputeRequest request, TwinChangesCollector collector, Properties properties) throws ServiceException {
        TwinEntity subscriberTwin = request.subscriberTwin();
        TwinStatusEntity oldStatus = subscriberTwin.getTwinStatus();
        TwinStatusEntity newStatus = twinStatusService.findEntitySafe(targetStatusId.extract(properties));
        if (collector.collectIfChanged(subscriberTwin, "status", subscriberTwin.getTwinStatusId(), newStatus.getId())) {
            collector.getHistoryCollector(subscriberTwin).add(historyService.statusChanged(oldStatus, newStatus));
            subscriberTwin
                    .setTwinStatusId(newStatus.getId())
                    .setTwinStatus(newStatus);
        }
    }
}
