package org.twins.core.service.history;

import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;
import org.twins.core.dao.history.context.HistoryContext;
import org.twins.core.enums.history.HistoryType;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
public class HistoryCollector {
    private List<Pair<HistoryType, HistoryContext>> historyList;

    public HistoryCollector add(HistoryType historyType, HistoryContext context) {
        if (historyList == null)
            historyList = new CopyOnWriteArrayList<>();
        historyList.add(Pair.of(historyType, context));
        return this;
    }

    public HistoryCollector add(HistoryItem<?> historyItem) {
        add(historyItem.getType(), historyItem.getContext());
        return this;
    }

    public HistoryCollector add(HistoryCollector otherCollector) {
        if (otherCollector == null)
            return this;
        if (historyList == null)
            historyList = new CopyOnWriteArrayList<>();
        historyList.addAll(otherCollector.getHistoryList());
        return this;
    }

    public boolean hasChanges() {
        return historyList != null && !historyList.isEmpty();
    }
}
