package org.twins.core.domain.factory;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.twins.core.domain.enum_.factory.Action;

@Data
@AllArgsConstructor
public class EraseAction {
    private Action action = Action.NOT_SPECIFIED;
    private String details = "";

    public EraseAction setAction(Action newDeletionMaker) {
        if (action == null
                || action == Action.NOT_SPECIFIED
                || action == Action.ERASE_CANDIDATE)
            this.action = newDeletionMaker;
        // all other actions can not be overridden
        return this;
    }

    public boolean isCauseGlobalLock() {
        return action == Action.RESTRICT;
    }
}
