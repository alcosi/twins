package org.twins.core.domain;

import lombok.Getter;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.service.history.HistoryCollector;
import org.twins.core.service.history.HistoryCollectorMultiTwin;

@Getter
public class TwinChangesCollector extends EntitiesChangesCollector {
    final HistoryCollectorMultiTwin historyCollector = new HistoryCollectorMultiTwin();
    boolean historyCollectorEnabled = true; // in some cases we do not need to collect history changes (before drafting for example, currently we do not collect history, only after )

    public TwinChangesCollector() {
        super();
    }

    public TwinChangesCollector(boolean historyCollectorEnabled) {
        super();
        this.historyCollectorEnabled = historyCollectorEnabled;
    }

    public HistoryCollector getHistoryCollector(TwinEntity twinEntity) {
        return historyCollector.forTwin(twinEntity);
    }

    public void clear() {
        super.clear();
        historyCollector.clear();
    }
}
