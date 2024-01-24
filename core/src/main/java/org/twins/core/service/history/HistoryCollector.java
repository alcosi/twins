package org.twins.core.service.history;

import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;
import org.twins.core.dao.history.HistoryType;
import org.twins.core.dao.history.context.HistoryContext;

import java.util.ArrayList;
import java.util.List;

@Getter
public class HistoryCollector {
    private List<Pair<HistoryType, HistoryContext>> historyList;

    public HistoryCollector add(HistoryType historyType, HistoryContext context) {
        if (historyList == null)
            historyList = new ArrayList<>();
        historyList.add(Pair.of(historyType, context));
        return this;
    }
}
