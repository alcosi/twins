package org.twins.core.featurer.fieldtyper.storage;

import lombok.RequiredArgsConstructor;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.util.KitUtils;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldBooleanEntity;
import org.twins.core.dao.twin.TwinFieldBooleanRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TwinFieldStorageBoolean implements TwinFieldStorage {
    private final TwinFieldBooleanRepository twinFieldBooleanRepository;

    @Override
    public void load(Kit<TwinEntity, UUID> twinsKit) {
        KitGrouped<TwinFieldBooleanEntity, UUID, UUID> allTwinsFieldGrouped = new KitGrouped<>(
                twinFieldBooleanRepository.findByTwinIdIn(twinsKit.getIdSet()), TwinFieldBooleanEntity::getId, TwinFieldBooleanEntity::getTwinId);
        if (!KitUtils.isEmpty(allTwinsFieldGrouped)) {
            for (var twinEntity : twinsKit) {
                twinEntity.setTwinFieldBooleanKit(new Kit<>(allTwinsFieldGrouped.getGrouped(twinEntity.getId()), TwinFieldBooleanEntity::getTwinClassFieldId));
            }
        }
    }

    @Override
    public boolean hasStrictValues(UUID twinClassFieldId) {
        return twinFieldBooleanRepository.existsByTwinClassFieldId(twinClassFieldId);
    }

    @Override
    public boolean isLoaded(TwinEntity twinEntity) {
        return twinEntity.getTwinFieldBooleanKit() != null;
    }

    @Override
    public void initEmpty(TwinEntity twinEntity) {
        twinEntity.setTwinFieldBooleanKit(Kit.EMPTY);
    }
}
