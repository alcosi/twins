package org.twins.core.featurer.fieldtyper.storage;

import lombok.RequiredArgsConstructor;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.util.KitUtils;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldSimpleEntity;
import org.twins.core.dao.twin.TwinFieldSimpleRepository;

import java.util.Properties;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TwinFieldStorageSimple implements TwinFieldStorage {
    private final TwinFieldSimpleRepository twinFieldSimpleRepository;

    @Override
    public void load(Kit<TwinEntity, UUID> twinsKit, Properties properties) {
        KitGrouped<TwinFieldSimpleEntity, UUID, UUID> allTwinsFieldGrouped = new KitGrouped<>(
                twinFieldSimpleRepository.findByTwinIdIn(twinsKit.getIdSet()), TwinFieldSimpleEntity::getId, TwinFieldSimpleEntity::getTwinId);
        if (!KitUtils.isEmpty(allTwinsFieldGrouped)) {
            for (var twinEntity : twinsKit) {
                twinEntity.setTwinFieldSimpleKit(new Kit<>(allTwinsFieldGrouped.getGrouped(twinEntity.getId()), TwinFieldSimpleEntity::getTwinClassFieldId));
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
}
