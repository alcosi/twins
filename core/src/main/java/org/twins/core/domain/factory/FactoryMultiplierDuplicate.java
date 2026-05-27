package org.twins.core.domain.factory;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.dao.factory.TwinFactoryMultiplierEntity;

import java.util.UUID;

@Data
@Accessors(chain = true)
public class FactoryMultiplierDuplicate {
    private UUID originalFactoryMultiplierId;
    private UUID newTwinFactoryId;
    private UUID newFactoryMultiplierId;

    private TwinFactoryMultiplierEntity originalFactoryMultiplier;
    private TwinFactoryEntity newTwinFactory;
}
