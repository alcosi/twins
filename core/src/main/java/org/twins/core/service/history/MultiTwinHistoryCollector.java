package org.twins.core.service.history;

import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;
import org.twins.core.dao.history.HistoryType;
import org.twins.core.dao.history.context.HistoryContext;
import org.twins.core.dao.twin.TwinEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
public class MultiTwinHistoryCollector {
    private Map<UUID, Pair<TwinEntity, HistoryCollector>> multiTwinHistory;
    public MultiTwinHistoryCollector add(TwinEntity twinEntity, HistoryType historyType, HistoryContext context) {
        if (multiTwinHistory == null)
            multiTwinHistory = new HashMap<>();
        HistoryCollector historyCollector = multiTwinHistory.computeIfAbsent(twinEntity.getId(), k -> Pair.of(twinEntity, new HistoryCollector())).getValue();
        historyCollector.add(historyType, context);
        return this;
    }
}
