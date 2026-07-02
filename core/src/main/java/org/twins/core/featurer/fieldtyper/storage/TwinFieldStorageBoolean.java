package org.twins.core.featurer.fieldtyper.storage;

import lombok.RequiredArgsConstructor;
import org.cambium.common.kit.Kit;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldBooleanEntity;
import org.twins.core.dao.twin.TwinFieldBooleanRepository;

import java.util.Collection;

@Component
@RequiredArgsConstructor
public class TwinFieldStorageBoolean extends TwinFieldStorageMater<TwinFieldBooleanEntity> {
    private final TwinFieldBooleanRepository twinFieldBooleanRepository;

    @Override
    protected TwinFieldBooleanRepository repository() {
        return twinFieldBooleanRepository;
    }

    @Override
    protected void assignKit(TwinEntity twin, Collection<TwinFieldBooleanEntity> entities) {
        twin.setTwinFieldBooleanKit(new Kit<>(entities, TwinFieldBooleanEntity::getTwinClassFieldId));
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
