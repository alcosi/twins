package org.twins.core.featurer.fieldtyper.storage;

import lombok.RequiredArgsConstructor;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldSimpleNonIndexedEntity;
import org.twins.core.dao.twin.TwinFieldSimpleNonIndexedRepository;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TwinFieldStorageSimpleNonIndex extends TwinFieldStorage {
    private final TwinFieldSimpleNonIndexedRepository twinFieldSimpleNonIndexedRepository;

    @Override
    public void load(Kit<TwinEntity, UUID> twinsKit) {
        KitGrouped<TwinFieldSimpleNonIndexedEntity, UUID, UUID> allTwinsFieldGrouped = new KitGrouped<>(
                twinFieldSimpleNonIndexedRepository.findByTwinIdIn(twinsKit.getIdSet()), TwinFieldSimpleNonIndexedEntity::getId, TwinFieldSimpleNonIndexedEntity::getTwinId);
        for (var twinEntity : twinsKit) {
            if (allTwinsFieldGrouped.containsGroupedKey(twinEntity.getId())) {
                twinEntity.setTwinFieldSimpleNonIndexedKit(new Kit<>(allTwinsFieldGrouped.getGrouped(twinEntity.getId()), TwinFieldSimpleNonIndexedEntity::getTwinClassFieldId));
            } else {
                initEmpty(twinEntity);
            }
        }
    }

    @Override
    public boolean hasStrictValues(UUID twinClassFieldId) {
        return twinFieldSimpleNonIndexedRepository.existsByTwinClassFieldId(twinClassFieldId);
    }

    @Override
    public boolean isLoaded(TwinEntity twinEntity) {
        return twinEntity.getTwinFieldSimpleNonIndexedKit() != null;
    }

    @Override
    public void initEmpty(TwinEntity twinEntity) {
        twinEntity.setTwinFieldSimpleNonIndexedKit(Kit.EMPTY);
    }

    @Override
    public Collection<UUID> findUsedFields(UUID twinClassId, Set<UUID> twinClassFieldIdSet) {
        return twinFieldSimpleNonIndexedRepository.findUsedFieldsByTwinClassIdAndTwinClassFieldIdIn(twinClassId, twinClassFieldIdSet);
    }

    @Override
    public void replaceTwinClassFieldForTwinsOfClass(UUID twinClassId, UUID fromTwinClassFieldId, UUID toTwinClassFieldId) {
        twinFieldSimpleNonIndexedRepository.replaceTwinClassFieldForTwinsOfClass(twinClassId, fromTwinClassFieldId, toTwinClassFieldId);
    }

    public void deleteTwinFieldsForTwins(Map<UUID, Set<UUID>> deleteMap) {
        //todo optimize for bulk delete
        for (var entry : deleteMap.entrySet())
            twinFieldSimpleNonIndexedRepository.deleteByTwinIdAndTwinClassFieldIdIn(entry.getKey(), entry.getValue());
    }

    @Override
    boolean canBeMerged(Object o) {
        return isSameClass(o);
    }
}
