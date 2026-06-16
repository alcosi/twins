package org.twins.core.domain.factory;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dao.factory.TwinFactoryMultiplierEntity;
import org.twins.core.domain.EntityDuplicate;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FactoryMultiplierDuplicate extends EntityDuplicate<TwinFactoryMultiplierEntity> {
    private boolean duplicateFilters = false;
}
