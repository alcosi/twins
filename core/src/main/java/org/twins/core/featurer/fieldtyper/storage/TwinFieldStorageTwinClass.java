package org.twins.core.featurer.fieldtyper.storage;

import lombok.RequiredArgsConstructor;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.*;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TwinFieldStorageTwinClass extends TwinFieldStorage {

    private final TwinFieldTwinClassRepository twinFieldTwinClassRepository;

    @Override
    public void load(Kit<TwinEntity, UUID> twinsKit) {
        KitGrouped<TwinFieldTwinClassListEntity, UUID, UUID> allTwinsFieldGrouped = new KitGrouped<>(
                twinFieldTwinClassRepository.findByTwinIdIn(twinsKit.getIdSet()), TwinFieldTwinClassListEntity::getId, TwinFieldTwinClassListEntity::getTwinId);
        for (var twinEntity : twinsKit) {
            if (allTwinsFieldGrouped.containsGroupedKey(twinEntity.getId())) {
                twinEntity.setTwinFieldTwinClassListKit(new Kit<>(allTwinsFieldGrouped.getGrouped(twinEntity.getId()), TwinFieldTwinClassListEntity::getTwinClassFieldId));
            } else {
                initEmpty(twinEntity);
            }
        }
    }

    @Override
    public boolean hasStrictValues(UUID twinClassFieldId) {
        return twinFieldTwinClassRepository.existsByTwinClassFieldId(twinClassFieldId);
    }

    @Override
    public boolean isLoaded(TwinEntity twinEntity) {
        return twinEntity.getTwinFieldBooleanKit() != null;
    }

    @Override
    public void initEmpty(TwinEntity twinEntity) {
        twinEntity.setTwinFieldBooleanKit(Kit.EMPTY);
    }

    @Override
    public Collection<UUID> findUsedFields(UUID twinClassId, Set<UUID> twinClassFieldIdSet) {
        return twinFieldTwinClassRepository.findUsedFieldsByTwinClassIdAndTwinClassFieldIdIn(twinClassId, twinClassFieldIdSet);
    }

    @Override
    public void replaceTwinClassFieldForTwinsOfClass(UUID twinClassId, UUID fromTwinClassFieldId, UUID toTwinClassFieldId) {
        twinFieldTwinClassRepository.replaceTwinClassFieldForTwinsOfClass(twinClassId, fromTwinClassFieldId, toTwinClassFieldId);
    }

    @Override
    boolean canBeMerged(Object o) {
        return isSameClass(o);
    }
}
