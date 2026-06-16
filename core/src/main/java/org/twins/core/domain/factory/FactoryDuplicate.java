package org.twins.core.domain.factory;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.domain.EntityDuplicate;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FactoryDuplicate extends EntityDuplicate<TwinFactoryEntity> {
    private UUID newFactoryId;
    private boolean duplicateBranches = false;
    private boolean duplicateMultipliers = false;
    private boolean duplicatePipelines = false;
    private boolean duplicateErasers = false;
    private boolean duplicateTriggers = false;
    private boolean duplicateConditionSets = false;
}
