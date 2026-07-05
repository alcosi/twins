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

    @Override
    protected TwinEntity resolveTwin(HistoryEntity history) throws ServiceException {
        TwinEntity headTwin = null;
        TwinEntity twin = history.getTwin();

        if (twin != null) { //todo logic if null
            headTwin = twin.getHeadTwin();
            if (headTwin == null && twin.getHeadTwinId() != null) {
                headTwin = twinService.findEntitySafe(twin.getHeadTwinId());
            }
        }

        return headTwin;
    }
}
