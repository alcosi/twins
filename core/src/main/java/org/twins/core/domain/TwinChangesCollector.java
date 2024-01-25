package org.twins.core.domain;

import lombok.Getter;
import org.twins.core.service.history.HistoryCollectorMultiTwin;

@Getter
public class TwinChangesCollector extends EntitiesChangesCollector {
    public TwinChangesCollector() {
        super();
    }

    HistoryCollectorMultiTwin historyCollector = new HistoryCollectorMultiTwin();
}
