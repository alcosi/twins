package org.twins.core.featurer.fieldtyper.storage;

import lombok.RequiredArgsConstructor;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.util.KitUtils;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldDataListEntity;
import org.twins.core.dao.twin.TwinFieldDataListRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TwinFieldStorageDatalist implements TwinFieldStorage {
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
}
