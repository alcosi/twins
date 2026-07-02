package org.twins.core.featurer.fieldtyper.storage;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldBaseEntity;
import org.twins.core.dao.twin.TwinFieldRepository;

import java.util.*;

public abstract class TwinFieldStorageMater<E extends TwinFieldBaseEntity> extends TwinFieldStorage {

    protected abstract TwinFieldRepository<E> repository();

    protected abstract void assignKit(TwinEntity twin, Collection<E> entities);

    @Override
    public void load(Kit<TwinEntity, UUID> twinsKit) throws ServiceException {
        KitGrouped<E, UUID, UUID> allTwinsFieldGrouped = new KitGrouped<>(
                repository().findByTwinIdIn(twinsKit.getIdSet()),
                TwinFieldBaseEntity::getId,
                TwinFieldBaseEntity::getTwinId);
        for (TwinEntity twinEntity : twinsKit) {
            if (allTwinsFieldGrouped.containsGroupedKey(twinEntity.getId())) {
                var fields = allTwinsFieldGrouped.getGrouped(twinEntity.getId());
                injectRelations(fields, twinEntity);
                assignKit(twinEntity, fields);
            } else {
                initEmpty(twinEntity);
            }
        }
    }

    protected void injectRelations(List<E> twinFields, TwinEntity twinEntity) {
        for (var twinField : twinFields) {
            twinField
                    .setTwin(twinEntity)
                    .setTwinClassField(twinEntity.getTwinClass().getTwinClassFieldKit().get(twinField.getTwinClassFieldId()));
        }
    }

    @Override
    public boolean hasStrictValues(UUID twinClassFieldId) {
        return repository().existsByTwinClassFieldId(twinClassFieldId);
    }

    @Override
    public Collection<UUID> findUsedFields(UUID twinClassId, Set<UUID> twinClassFieldIdSet) {
        return repository().findUsedFieldsByTwinClassIdAndTwinClassFieldIdIn(twinClassId, twinClassFieldIdSet);
    }

    @Override
    public void replaceTwinClassFieldForTwinsOfClass(UUID twinClassId, UUID fromTwinClassFieldId, UUID toTwinClassFieldId) {
        repository().replaceTwinClassFieldForTwinsOfClass(twinClassId, fromTwinClassFieldId, toTwinClassFieldId);
    }

    @Override
    public void deleteTwinFieldsForTwins(Map<UUID, Set<UUID>> deleteMap) {
        for (Map.Entry<UUID, Set<UUID>> entry : deleteMap.entrySet()) {
            repository().deleteByTwinIdAndTwinClassFieldIdIn(entry.getKey(), entry.getValue());
        }
    }

    @Override
    boolean canBeMerged(Object o) {
        return isSameClass(o);
    }
}
