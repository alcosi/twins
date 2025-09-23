package org.twins.core.featurer.fieldtyper.storage;

import lombok.RequiredArgsConstructor;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldUserEntity;
import org.twins.core.dao.twin.TwinFieldUserRepository;

import java.util.*;

@Component
@RequiredArgsConstructor
public class TwinFieldStorageUser extends TwinFieldStorage {
    private final TwinFieldUserRepository twinFieldUserRepository;

    @Override
    public void load(Kit<TwinEntity, UUID> twinsKit) {
        KitGrouped<TwinFieldUserEntity, UUID, UUID> allTwinsFieldGrouped = new KitGrouped<>(
                twinFieldUserRepository.findByTwinIdIn(twinsKit.getIdSet()), TwinFieldUserEntity::getId, TwinFieldUserEntity::getTwinId);
        for (var twinEntity : twinsKit) {
            if (allTwinsFieldGrouped.containsGroupedKey(twinEntity.getId())) {
                twinEntity.setTwinFieldUserKit(new KitGrouped<>(allTwinsFieldGrouped.getGrouped(twinEntity.getId()), TwinFieldUserEntity::getId, TwinFieldUserEntity::getTwinClassFieldId));
            } else {
                initEmpty(twinEntity);
            }
        }
    }

    @Override
    public boolean hasStrictValues(UUID twinClassFieldId) {
        return twinFieldUserRepository.existsByTwinClassFieldId(twinClassFieldId);
    }

    @Override
    public boolean isLoaded(TwinEntity twinEntity) {
        return twinEntity.getTwinFieldUserKit() != null;
    }

    @Override
    public void initEmpty(TwinEntity twinEntity) {
        twinEntity.setTwinFieldUserKit(KitGrouped.EMPTY);
    }

    @Override
    public Collection<UUID> findUsedFields(UUID twinClassId, Set<UUID> twinClassFieldIdSet) {
        return Collections.EMPTY_LIST;
    }

    @Override
    boolean canBeMerged(Object o) {
        return isSameClass(o);
    }

    @Override
    public void replaceTwinClassFieldForTwinsOfClass(UUID twinClassId, UUID fromTwinClassFieldId, UUID toTwinClassFieldId) {
        //nothing to replace
    }

    @Override
    public void deleteTwinFieldsForTwins(Map<UUID, Set<UUID>> deleteMap) {
        //todo optimize for bulk delete
        for (var entry : deleteMap.entrySet())
            twinFieldUserRepository.deleteByTwinIdAndTwinClassFieldIdIn(entry.getKey(), entry.getValue());
    }

}
