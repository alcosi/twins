package org.twins.core.service.history;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.twins.core.enums.history.HistoryType;
import org.twins.core.dao.history.context.HistoryContext;

@Getter
@AllArgsConstructor
public class HistoryItem<T extends HistoryContext> {
    private HistoryType type;
    private T context;
}
