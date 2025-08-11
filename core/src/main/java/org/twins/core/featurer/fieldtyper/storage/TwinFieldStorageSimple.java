package org.twins.core.featurer.fieldtyper.storage;

import lombok.RequiredArgsConstructor;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldSimpleEntity;
import org.twins.core.dao.twin.TwinFieldSimpleRepository;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TwinFieldStorageSimple extends TwinFieldStorage {
    private final TwinFieldSimpleRepository twinFieldSimpleRepository;

    @Override
    public void load(Kit<TwinEntity, UUID> twinsKit) {
        KitGrouped<TwinFieldSimpleEntity, UUID, UUID> allTwinsFieldGrouped = new KitGrouped<>(
                twinFieldSimpleRepository.findByTwinIdIn(twinsKit.getIdSet()), TwinFieldSimpleEntity::getId, TwinFieldSimpleEntity::getTwinId);
        for (var twinEntity : twinsKit) {
            if (allTwinsFieldGrouped.containsGroupedKey(twinEntity.getId())) {
                twinEntity.setTwinFieldSimpleKit(new Kit<>(allTwinsFieldGrouped.getGrouped(twinEntity.getId()), TwinFieldSimpleEntity::getTwinClassFieldId));
            } else {
                initEmpty(twinEntity);
            }
        }
    }

    @Override
    public boolean hasStrictValues(UUID twinClassFieldId) {
        return twinFieldSimpleRepository.existsByTwinClassFieldId(twinClassFieldId);
    }

    @Override
    public boolean isLoaded(TwinEntity twinEntity) {
        return twinEntity.getTwinFieldSimpleKit() != null;
    }

    @Override
    public void initEmpty(TwinEntity twinEntity) {
        twinEntity.setTwinFieldSimpleKit(Kit.EMPTY);
    }

    @Override
    public Collection<UUID> findUsedFields(UUID twinClassId, Set<UUID> twinClassFieldIdSet) {
        return twinFieldSimpleRepository.findUsedFieldsByTwinClassIdAndTwinClassFieldIdIn(twinClassId, twinClassFieldIdSet);
    }

    @Override
    public void replaceTwinClassFieldForTwinsOfClass(UUID twinClassId, UUID fromTwinClassFieldId, UUID toTwinClassFieldId) {
        twinFieldSimpleRepository.replaceTwinClassFieldForTwinsOfClass(twinClassId, fromTwinClassFieldId, toTwinClassFieldId);
    }

    public void deleteTwinFieldsForTwins(Map<UUID, Set<UUID>> deleteMap) {
        //todo optimize for bulk delete
        for (var entry : deleteMap.entrySet())
            twinFieldSimpleRepository.deleteByTwinIdAndTwinClassFieldIdIn(entry.getKey(), entry.getValue());
    }

    @Override
    boolean canBeMerged(Object o) {
        return isSameClass(o);
    }
}
