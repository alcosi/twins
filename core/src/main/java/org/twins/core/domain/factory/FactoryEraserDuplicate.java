package org.twins.core.domain.factory;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.dao.factory.TwinFactoryEraserEntity;

import java.util.UUID;

@Data
@Accessors(chain = true)
public class FactoryEraserDuplicate {
    private UUID originalFactoryEraserId;
    private UUID newTwinFactoryId;
    private UUID newFactoryEraserId;

    private TwinFactoryEraserEntity originalFactoryEraser;
    private TwinFactoryEntity newTwinFactory;
}
