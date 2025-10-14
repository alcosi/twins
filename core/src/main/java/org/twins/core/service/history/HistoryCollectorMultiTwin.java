package org.twins.core.service.history;

import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;
import org.twins.core.enums.history.HistoryType;
import org.twins.core.dao.history.context.HistoryContext;
import org.twins.core.dao.twin.TwinEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
public class HistoryCollectorMultiTwin {
    private Map<UUID, Pair<TwinEntity, HistoryCollector>> multiTwinHistory;

    public HistoryCollector forTwin(TwinEntity twinEntity) {
        if (multiTwinHistory == null)
            multiTwinHistory = new HashMap<>();
        return multiTwinHistory.computeIfAbsent(twinEntity.getId(), k -> Pair.of(twinEntity, new HistoryCollector())).getValue();
    }

    public HistoryCollectorMultiTwin add(TwinEntity twinEntity, HistoryType historyType, HistoryContext context) {
        if (multiTwinHistory == null)
            multiTwinHistory = new HashMap<>();
        HistoryCollector historyCollector = multiTwinHistory.computeIfAbsent(twinEntity.getId(), k -> Pair.of(twinEntity, new HistoryCollector())).getValue();
        historyCollector.add(historyType, context);
        return this;
    }

    public HistoryCollectorMultiTwin add(HistoryCollectorMultiTwin otherCollector) {
        if (otherCollector.getMultiTwinHistory() == null)
            return this;
        if (multiTwinHistory == null)
            multiTwinHistory = new HashMap<>();
        for (Map.Entry<UUID, Pair<TwinEntity, HistoryCollector>> entry : otherCollector.getMultiTwinHistory().entrySet()) {
            if (multiTwinHistory.containsKey(entry.getKey())) {
                multiTwinHistory.get(entry.getKey()).getValue().add(entry.getValue().getValue());
            } else {
                multiTwinHistory.put(entry.getKey(), entry.getValue());
            }
        }
        return this;
    }

    public void clear() {
        if (multiTwinHistory != null)
            multiTwinHistory.clear();
    }
}
