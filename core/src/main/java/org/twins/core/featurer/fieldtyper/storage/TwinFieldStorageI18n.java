package org.twins.core.featurer.fieldtyper.storage;

import lombok.RequiredArgsConstructor;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.util.KitUtils;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldI18nEntity;
import org.twins.core.dao.twin.TwinFieldI18nRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TwinFieldStorageI18n implements TwinFieldStorage {
    private final TwinFieldI18nRepository twinFieldI18nRepository;

    @Override
    public void load(Kit<TwinEntity, UUID> twinsKit) {
        KitGrouped<TwinFieldI18nEntity, UUID, UUID> allTwinsFieldGrouped = new KitGrouped<>(
                twinFieldI18nRepository.findByTwinIdIn(twinsKit.getIdSet()), TwinFieldI18nEntity::getId, TwinFieldI18nEntity::getTwinId);
        if (!KitUtils.isEmpty(allTwinsFieldGrouped)) {
            for (var twinEntity : twinsKit) {
                twinEntity.setTwinFieldI18nKit(new KitGrouped<>(allTwinsFieldGrouped.getGrouped(twinEntity.getId()), TwinFieldI18nEntity::getId, TwinFieldI18nEntity::getTwinClassFieldId));
            }
        }
    }

    @Override
    public boolean hasStrictValues(UUID twinClassFieldId) {
        return twinFieldI18nRepository.existsByTwinClassFieldId(twinClassFieldId);
    }

    @Override
    public boolean isLoaded(TwinEntity twinEntity) {
        return twinEntity.getTwinFieldI18nKit() != null;
    }

    @Override
    public void initEmpty(TwinEntity twinEntity) {
        twinEntity.setTwinFieldI18nKit(KitGrouped.EMPTY);
    }
}
