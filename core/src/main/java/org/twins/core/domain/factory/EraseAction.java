package org.twins.core.domain.factory;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.twins.core.dao.factory.TwinFactoryEraserEntity;

@Data
@AllArgsConstructor
public class EraseAction {
    private TwinFactoryEraserEntity.Action action = TwinFactoryEraserEntity.Action.NOT_SPECIFIED;
    private String details = "";

    public EraseAction setAction(TwinFactoryEraserEntity.Action newDeletionMaker) {
        if (action == null
                || action == TwinFactoryEraserEntity.Action.NOT_SPECIFIED
                || action == TwinFactoryEraserEntity.Action.ERASE_CANDIDATE)
            this.action = newDeletionMaker;
        // all other actions can not be overridden
        return this;
    }

    public boolean isCauseGlobalLock() {
        return action == TwinFactoryEraserEntity.Action.RESTRICT;
    }
}
