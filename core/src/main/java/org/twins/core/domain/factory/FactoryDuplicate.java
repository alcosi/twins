package org.twins.core.domain.factory;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.factory.TwinFactoryEntity;

import java.util.UUID;

@Data
@Accessors(chain = true)
public class FactoryDuplicate {
    private UUID originalFactoryId;
    private UUID newFactoryId;
    private String newKey;
    private boolean duplicateBranches = false;
    private boolean duplicateMultipliers = false;
    private boolean duplicatePipelines = false;
    private boolean duplicateErasers = false;
    private boolean duplicateTriggers = false;

    private TwinFactoryEntity originalFactory;
    private TwinFactoryEntity newFactory;
}
