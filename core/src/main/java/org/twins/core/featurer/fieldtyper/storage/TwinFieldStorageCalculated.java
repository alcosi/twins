package org.twins.core.featurer.fieldtyper.storage;

import lombok.RequiredArgsConstructor;
import org.cambium.common.kit.Kit;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldSimpleRepository;

import java.util.Properties;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
public class TwinFieldStorageCalculated implements TwinFieldStorage {
    private final TwinFieldSimpleRepository twinFieldSimpleRepository;
    private final UUID childrenTwinClassFieldId;
    private final Set<UUID> childrenTwinStatusIdSet;
    private final boolean exclude;

    @Override
    public void load(Kit<TwinEntity, UUID> twinsKit, Properties properties) {
        exclude ?
                twinFieldSimpleRepository.sumChildrenTwinFieldValuesWithStatusNotIn(twinEntity.getId(), childrenTwinClassFieldId.extract(properties), childrenTwinStatusIdSet.extract(properties)) :
                twinFieldSimpleRepository.sumChildrenTwinFieldValuesWithStatusIn(twinEntity.getId(), childrenTwinClassFieldId.extract(properties), childrenTwinStatusIdSet.extract(properties));
    }

    @Override
    public boolean hasStrictValues(UUID twinClassFieldId) {
        return false;
    }

    @Override
    public boolean isLoaded(TwinEntity twinEntity) {
        return true;
    }

    @Override
    public void initEmpty(TwinEntity twinEntity) {

    }
}
