package org.twins.core.domain.factory;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.dao.factory.TwinFactoryTriggerEntity;
import org.twins.core.domain.EntityDuplicate;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FactoryTriggerDuplicate extends EntityDuplicate<TwinFactoryTriggerEntity, TwinFactoryEntity> {
}
