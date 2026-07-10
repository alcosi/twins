package org.twins.core.featurer.fieldtyper.storage;

import lombok.RequiredArgsConstructor;
import org.cambium.common.kit.KitGrouped;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldI18nEntity;
import org.twins.core.dao.twin.TwinFieldI18nRepository;

import java.util.Collection;

@Component
@RequiredArgsConstructor
public class TwinFieldStorageI18n extends TwinFieldStorageMater<TwinFieldI18nEntity> {
    private final TwinFieldI18nRepository twinFieldI18nRepository;

    @Override
    protected TwinFieldI18nRepository repository() {
        return twinFieldI18nRepository;
    }

    @Override
    protected void assignKit(TwinEntity twin, Collection<TwinFieldI18nEntity> entities) {
        twin.setTwinFieldI18nKit(new KitGrouped<>(entities, TwinFieldI18nEntity::getId, TwinFieldI18nEntity::getTwinClassFieldId));
    }

    @Override
    public boolean isLoaded(TwinEntity twinEntity) {
        return twinEntity.getTwinFieldI18nKit() != null;
    }

    @Override
    public void initEmpty(TwinEntity twinEntity) {
        twinEntity.setTwinFieldI18nKit(new KitGrouped<>(TwinFieldI18nEntity::getId, TwinFieldI18nEntity::getTwinClassFieldId));  //not empty kit, because it's immutable, and we need kit update on field serialization
    }
}
