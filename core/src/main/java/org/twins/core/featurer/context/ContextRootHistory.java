package org.twins.core.featurer.context;

import lombok.Getter;
import org.twins.core.dao.history.HistoryEntity;

/**
 * {@link ContextRoot} for history-driven collection (the notification pipeline): the root twin plus
 * the history record itself, for history-scoped collectors (comment, status change, actor user).
 */
@Getter
public class ContextRootHistory extends ContextRoot {
    private final HistoryEntity history;

    public ContextRootHistory(HistoryEntity history) {
        super(history.getTwin());
        this.history = history;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return history.equals(((ContextRootHistory) o).history);
    }

    @Override
    public int hashCode() {
        return history.hashCode();
    }
}
