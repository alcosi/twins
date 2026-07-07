package org.twins.core.featurer.fieldtyper.storage;

import lombok.RequiredArgsConstructor;
import org.cambium.common.kit.KitGrouped;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldDataListEntity;
import org.twins.core.dao.twin.TwinFieldDataListRepository;

import java.util.Collection;

@Component
@RequiredArgsConstructor
public class TwinFieldStorageDatalist extends TwinFieldStorageMater<TwinFieldDataListEntity> {
    private final TwinFieldDataListRepository twinFieldDataListRepository;

    @Override
    protected TwinFieldDataListRepository repository() {
        return twinFieldDataListRepository;
    }

    @Override
    protected void assignKit(TwinEntity twin, Collection<TwinFieldDataListEntity> entities) {
        twin.setTwinFieldDatalistKit(new KitGrouped<>(entities, TwinFieldDataListEntity::getId, TwinFieldDataListEntity::getTwinClassFieldId));
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
