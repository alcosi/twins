package org.twins.core.domain.factory;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.factory.TwinFactoryBranchEntity;
import org.twins.core.dao.factory.TwinFactoryEntity;

import java.util.UUID;

@Data
@Accessors(chain = true)
public class FactoryBranchDuplicate {
    private UUID originalFactoryBranchId;
    private UUID newTwinFactoryId;
    private UUID newFactoryBranchId;

    private TwinFactoryBranchEntity originalFactoryBranch;
    private TwinFactoryEntity newTwinFactory;
}
