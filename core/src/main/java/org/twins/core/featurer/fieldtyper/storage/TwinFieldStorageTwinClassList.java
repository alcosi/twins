package org.twins.core.featurer.fieldtyper.storage;

import lombok.RequiredArgsConstructor;
import org.cambium.common.kit.KitGrouped;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldTwinClassEntity;
import org.twins.core.dao.twin.TwinFieldTwinClassListRepository;

import java.util.Collection;

@Component
@RequiredArgsConstructor
public class TwinFieldStorageTwinClassList extends TwinFieldStorageMater<TwinFieldTwinClassEntity> {

    private final TwinFieldTwinClassListRepository twinFieldTwinClassListRepository;

    @Override
    protected TwinFieldTwinClassListRepository repository() {
        return twinFieldTwinClassListRepository;
    }

    @Override
    protected void assignKit(TwinEntity twin, Collection<TwinFieldTwinClassEntity> entities) {
        twin.setTwinFieldTwinClassKit(new KitGrouped<>(entities, TwinFieldTwinClassEntity::getId, TwinFieldTwinClassEntity::getTwinClassFieldId));
    }

    @Override
    public boolean isLoaded(TwinEntity twinEntity) {
        return twinEntity.getTwinFieldTwinClassKit() != null;
    }

    @Override
    public void initEmpty(TwinEntity twinEntity) {
        twinEntity.setTwinFieldTwinClassKit(new KitGrouped<>(TwinFieldTwinClassEntity::getId, TwinFieldTwinClassEntity::getTwinClassFieldId)); //not empty kit, because it's immutable, and we need kit update on field serialization
    }
}
