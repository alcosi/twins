package org.twins.core.featurer.fieldtyper.storage;

import org.cambium.common.kit.Kit;
import org.twins.core.dao.twin.TwinEntity;

import java.util.UUID;

public interface TwinFieldStorage {
    void load(Kit<TwinEntity, UUID> twinsKit);

    boolean hasStrictValues(UUID twinClassFieldId);

    boolean isLoaded(TwinEntity twinEntity);

    void initEmpty(TwinEntity twinEntity);

}
