package org.twins.core.domain.factory;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.twins.core.enums.factory.FactoryEraserAction;

@Data
@AllArgsConstructor
public class EraseAction {
    private FactoryEraserAction action = FactoryEraserAction.NOT_SPECIFIED;
    private String details = "";

    public EraseAction setAction(FactoryEraserAction newDeletionMaker) {
        if (action == null
                || action == FactoryEraserAction.NOT_SPECIFIED
                || action == FactoryEraserAction.ERASE_CANDIDATE)
            this.action = newDeletionMaker;
        // all other actions can not be overridden
        return this;
    }

    public boolean isCauseGlobalLock() {
        return action == FactoryEraserAction.RESTRICT;
    }
}
