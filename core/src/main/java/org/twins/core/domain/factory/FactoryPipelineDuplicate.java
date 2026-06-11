package org.twins.core.domain.factory;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.dao.factory.TwinFactoryPipelineEntity;

import java.util.UUID;

@Data
@Accessors(chain = true)
public class FactoryPipelineDuplicate {
    private UUID originalFactoryPipelineId;
    private UUID newTwinFactoryId;
    private UUID newFactoryPipelineId;

    private TwinFactoryPipelineEntity originalFactoryPipeline;
    private TwinFactoryEntity newTwinFactory;
}
