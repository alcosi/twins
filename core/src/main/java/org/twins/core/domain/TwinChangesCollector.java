package org.twins.core.domain;

import lombok.Getter;
import org.twins.core.service.history.MultiTwinHistoryCollector;

@Getter
public class TwinChangesCollector extends EntitiesChangesCollector {
    public TwinChangesCollector() {
        super();
    }

    MultiTwinHistoryCollector historyCollector = new MultiTwinHistoryCollector();
}
