package org.twins.core.domain.factory;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dao.factory.TwinFactoryBranchEntity;
import org.twins.core.domain.EntityDuplicate;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FactoryBranchDuplicate extends EntityDuplicate<TwinFactoryBranchEntity> {
    private UUID newTwinFactoryId;
}
