package org.twins.core.featurer.fieldtyper.storage;

import lombok.RequiredArgsConstructor;
import org.cambium.common.kit.Kit;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldSimpleEntity;
import org.twins.core.dao.twin.TwinFieldSimpleRepository;

import java.util.Collection;

@Component
@RequiredArgsConstructor
public class TwinFieldStorageSimple extends TwinFieldStorageMater<TwinFieldSimpleEntity> {
    private final TwinFieldSimpleRepository twinFieldSimpleRepository;

    @Override
    protected TwinFieldSimpleRepository repository() {
        return twinFieldSimpleRepository;
    }

    @Override
    protected void assignKit(TwinEntity twin, Collection<TwinFieldSimpleEntity> entities) {
        twin.setTwinFieldSimpleKit(new Kit<>(entities, TwinFieldSimpleEntity::getTwinClassFieldId));
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
