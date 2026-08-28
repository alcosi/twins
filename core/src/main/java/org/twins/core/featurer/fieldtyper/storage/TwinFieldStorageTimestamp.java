package org.twins.core.featurer.fieldtyper.storage;

import lombok.RequiredArgsConstructor;
import org.cambium.common.kit.Kit;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldTimestampEntity;
import org.twins.core.dao.twin.TwinFieldTimestampRepository;

import java.util.Collection;

@Component
@RequiredArgsConstructor
public class TwinFieldStorageTimestamp extends TwinFieldStorageMater<TwinFieldTimestampEntity> {
    private final TwinFieldTimestampRepository twinFieldTimestampRepository;

    @Override
    protected TwinFieldTimestampRepository repository() {
        return twinFieldTimestampRepository;
    }

    @Override
    protected void assignKit(TwinEntity twin, Collection<TwinFieldTimestampEntity> entities) {
        twin.setTwinFieldTimestampKit(new Kit<>(entities, TwinFieldTimestampEntity::getTwinClassFieldId));
    }

    @Override
    public boolean isLoaded(TwinEntity twinEntity) {
        return twinEntity.getTwinFieldTimestampKit() != null;
    }

    @Override
    public void initEmpty(TwinEntity twinEntity) {
        twinEntity.setTwinFieldTimestampKit(new Kit<>(TwinFieldTimestampEntity::getTwinClassFieldId)); //not empty kit, because it's immutable, and we need kit update on field serialization
    }
}
