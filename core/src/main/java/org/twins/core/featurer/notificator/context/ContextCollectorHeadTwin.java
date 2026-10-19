package org.twins.core.featurer.notificator.context;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.service.twin.TwinService;


@Component
@Featurer(id = FeaturerTwins.ID_4907,
        name = "Context collector head twin",
        description = "Collect form head twin (id, name, description)")
@Slf4j
public class ContextCollectorHeadTwin extends ContextCollectorTwinBase {

    @Lazy
    @Autowired
    private TwinService twinService;

    /**
     * Bulk-load head twins for the whole batch so {@link #resolveTwin} reads the relation in-memory
     * (was a per-history {@code findEntitySafe} fallback — N+1).
     */
    @Override
    protected void beforeCollect(ContextCollectorBatch batch) throws ServiceException {
        if (!batch.getTwins().isEmpty()) {
            twinService.loadHead(batch.getTwins());
        }
    }

    @Override
    protected TwinEntity resolveTwin(HistoryEntity history) throws ServiceException {
        return history.getTwin().getHeadTwin();
    }
}
