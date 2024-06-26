package org.twins.core.domain;

import lombok.Getter;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.service.history.HistoryCollector;
import org.twins.core.service.history.HistoryCollectorMultiTwin;

@Getter
public class TwinChangesCollector extends EntitiesChangesCollector {
    public TwinChangesCollector() {
        super();
    }

    HistoryCollectorMultiTwin historyCollector = new HistoryCollectorMultiTwin();

    public HistoryCollector getHistoryCollector(TwinEntity twinEntity) {
        return historyCollector.forTwin(twinEntity);
    }
}
