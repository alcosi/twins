package org.twins.core.featurer.fieldtyper.storage;

import lombok.RequiredArgsConstructor;
import org.cambium.common.kit.Kit;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldSimpleNonIndexedEntity;
import org.twins.core.dao.twin.TwinFieldSimpleNonIndexedRepository;

import java.util.Collection;

@Component
@RequiredArgsConstructor
public class TwinFieldStorageSimpleNonIndex extends TwinFieldStorageMater<TwinFieldSimpleNonIndexedEntity> {
    private final TwinFieldSimpleNonIndexedRepository twinFieldSimpleNonIndexedRepository;

    @Override
    protected TwinFieldSimpleNonIndexedRepository repository() {
        return twinFieldSimpleNonIndexedRepository;
    }

    @Override
    protected void assignKit(TwinEntity twin, Collection<TwinFieldSimpleNonIndexedEntity> entities) {
        twin.setTwinFieldSimpleNonIndexedKit(new Kit<>(entities, TwinFieldSimpleNonIndexedEntity::getTwinClassFieldId));
    }

    @Override
    public boolean isLoaded(TwinEntity twinEntity) {
        return twinEntity.getTwinFieldSimpleNonIndexedKit() != null;
    }

    @Override
    public void initEmpty(TwinEntity twinEntity) {
        twinEntity.setTwinFieldSimpleNonIndexedKit(Kit.EMPTY);
    }
}
