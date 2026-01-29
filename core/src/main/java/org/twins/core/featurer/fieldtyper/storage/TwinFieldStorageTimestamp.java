package org.twins.core.featurer.fieldtyper.storage;

import lombok.RequiredArgsConstructor;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldTimestampEntity;
import org.twins.core.dao.twin.TwinFieldTimestampRepository;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TwinFieldStorageTimestamp extends TwinFieldStorage {
    private final TwinFieldTimestampRepository twinFieldTimestampRepository;

    @Override
    public void load(Kit<TwinEntity, UUID> twinsKit) {
        KitGrouped<TwinFieldTimestampEntity, UUID, UUID> allTwinsFieldGrouped = new KitGrouped<>(twinFieldTimestampRepository.findByTwinIdIn(twinsKit.getIdSet()), TwinFieldTimestampEntity::getId, TwinFieldTimestampEntity::getTwinId);
        for (var twinEntity : twinsKit) {
            if (allTwinsFieldGrouped.containsGroupedKey(twinEntity.getId())) {
                twinEntity.setTwinFieldTimestampKit(new Kit<>(allTwinsFieldGrouped.getGrouped(twinEntity.getId()), TwinFieldTimestampEntity::getTwinClassFieldId));
            } else {
                initEmpty(twinEntity);
            }
        }
    }

    @Override
    public boolean hasStrictValues(UUID twinClassFieldId) {
        return twinFieldTimestampRepository.existsByTwinClassFieldId(twinClassFieldId);
    }

    @Override
    public boolean isLoaded(TwinEntity twinEntity) {
        return twinEntity.getTwinFieldTimestampKit() != null;
    }

    @Override
    public void initEmpty(TwinEntity twinEntity) {
        twinEntity.setTwinFieldTimestampKit(Kit.EMPTY);
    }

    @Override
    public Collection<UUID> findUsedFields(UUID twinClassId, Set<UUID> twinClassFieldIdSet) {
        return twinFieldTimestampRepository.findUsedFieldsByTwinClassIdAndTwinClassFieldIdIn(twinClassId, twinClassFieldIdSet);
    }

    @Override
    public void replaceTwinClassFieldForTwinsOfClass(UUID twinClassId, UUID fromTwinClassFieldId, UUID toTwinClassFieldId) {
        twinFieldTimestampRepository.replaceTwinClassFieldForTwinsOfClass(twinClassId, fromTwinClassFieldId, toTwinClassFieldId);
    }

    @Override
    public void deleteTwinFieldsForTwins(Map<UUID, Set<UUID>> deleteMap) {
        //todo optimize for bulk delete
        for (var entry : deleteMap.entrySet())
            twinFieldTimestampRepository.deleteByTwinIdAndTwinClassFieldIdIn(entry.getKey(), entry.getValue());
    }

    @Override
    boolean canBeMerged(Object o) {
        return isSameClass(o);
    }
}
