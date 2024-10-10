package org.twins.core.domain.factory;

import lombok.Getter;
import org.twins.core.dao.factory.TwinFactoryEraserEntity;

@Getter
public class FactoryEraseAction {
    private TwinFactoryEraserEntity.Action eraseAction = TwinFactoryEraserEntity.Action.DO_NOT_ERASE;
    private String eraseActionReason = "";
}
