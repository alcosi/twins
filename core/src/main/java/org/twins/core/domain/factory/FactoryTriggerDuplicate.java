package org.twins.core.domain.factory;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.dao.factory.TwinFactoryTriggerEntity;

import java.util.UUID;

@Data
@Accessors(chain = true)
public class FactoryTriggerDuplicate {
    private UUID originalFactoryTriggerId;
    private UUID newTwinFactoryId;
    private UUID newFactoryTriggerId;

    private TwinFactoryTriggerEntity originalFactoryTrigger;
    private TwinFactoryEntity newTwinFactory;
}
