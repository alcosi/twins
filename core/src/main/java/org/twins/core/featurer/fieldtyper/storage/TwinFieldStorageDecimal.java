package org.twins.core.featurer.fieldtyper.storage;

import lombok.RequiredArgsConstructor;
import org.cambium.common.kit.Kit;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldDecimalEntity;
import org.twins.core.dao.twin.TwinFieldDecimalRepository;

import java.util.Collection;

@Component
@RequiredArgsConstructor
public class TwinFieldStorageDecimal extends TwinFieldStorageMater<TwinFieldDecimalEntity> {

    private final TwinFieldDecimalRepository twinFieldDecimalRepository;

    @Override
    protected TwinFieldDecimalRepository repository() {
        return twinFieldDecimalRepository;
    }

    @Override
    protected void assignKit(TwinEntity twin, Collection<TwinFieldDecimalEntity> entities) {
        twin.setTwinFieldDecimalKit(new Kit<>(entities, TwinFieldDecimalEntity::getTwinClassFieldId));
    }

    @Override
    public boolean isLoaded(TwinEntity twinEntity) {
        return twinEntity.getTwinFieldDecimalKit() != null;
    }

    @Override
    public void initEmpty(TwinEntity twinEntity) {
        twinEntity.setTwinFieldDecimalKit(Kit.EMPTY);
    }
}
