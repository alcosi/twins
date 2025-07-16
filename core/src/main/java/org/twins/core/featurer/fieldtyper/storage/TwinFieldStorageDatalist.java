package org.twins.core.featurer.fieldtyper.storage;

import lombok.RequiredArgsConstructor;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.util.KitUtils;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldDataListEntity;
import org.twins.core.dao.twin.TwinFieldDataListRepository;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TwinFieldStorageDatalist extends TwinFieldStorage {
    private final TwinFieldDataListRepository twinFieldDataListRepository;

    @Override
    public void load(Kit<TwinEntity, UUID> twinsKit) {
        KitGrouped<TwinFieldDataListEntity, UUID, UUID> allTwinsFieldGrouped = new KitGrouped<>(
                twinFieldDataListRepository.findByTwinIdIn(twinsKit.getIdSet()), TwinFieldDataListEntity::getId, TwinFieldDataListEntity::getTwinId);
        if (!KitUtils.isEmpty(allTwinsFieldGrouped)) {
            for (var twinEntity : twinsKit) {
                twinEntity.setTwinFieldDatalistKit(new KitGrouped<>(allTwinsFieldGrouped.getGrouped(twinEntity.getId()), TwinFieldDataListEntity::getId, TwinFieldDataListEntity::getTwinClassFieldId));
            }
        }
    }

    @Override
    public boolean hasStrictValues(UUID twinClassFieldId) {
        return twinFieldDataListRepository.existsByTwinClassFieldId(twinClassFieldId);
    }

    @Override
    public boolean isLoaded(TwinEntity twinEntity) {
        return twinEntity.getTwinFieldDatalistKit() != null;
    }

    @Override
    public void initEmpty(TwinEntity twinEntity) {
        twinEntity.setTwinFieldDatalistKit(KitGrouped.EMPTY);
    }

    @Override
    public Collection<UUID> findUsedFields(UUID twinClassId, Set<UUID> twinClassFieldIdSet) {
        return twinFieldDataListRepository.findUsedFieldsByTwinClassIdAndTwinClassFieldIdIn(twinClassId, twinClassFieldIdSet);
    }

    @Override
    boolean canBeMerged(Object o) {
        return isSameClass(o);
    }
}
