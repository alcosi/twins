package org.twins.core.featurer.fieldtyper.storage;

import lombok.RequiredArgsConstructor;
import org.cambium.common.kit.KitGrouped;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldUserEntity;
import org.twins.core.dao.twin.TwinFieldUserRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TwinFieldStorageUser extends TwinFieldStorageMater<TwinFieldUserEntity> {
    private final TwinFieldUserRepository twinFieldUserRepository;

    @Override
    protected TwinFieldUserRepository repository() {
        return twinFieldUserRepository;
    }

    @Override
    protected void assignKit(TwinEntity twin, Collection<TwinFieldUserEntity> entities) {
        twin.setTwinFieldUserKit(new KitGrouped<>(entities, TwinFieldUserEntity::getId, TwinFieldUserEntity::getTwinClassFieldId));
    }

    @Override
    public boolean isLoaded(TwinEntity twinEntity) {
        return twinEntity.getTwinFieldUserKit() != null;
    }

    @Override
    public void initEmpty(TwinEntity twinEntity) {
        twinEntity.setTwinFieldUserKit(new KitGrouped<>(TwinFieldUserEntity::getId, TwinFieldUserEntity::getTwinClassFieldId)); //not empty kit, because it's immutable, and we need kit update on field serialization
    }

    @Override
    public Collection<UUID> findUsedFields(UUID twinClassId, Set<UUID> twinClassFieldIdSet) {
        return Collections.emptyList();
    }

    @Override
    public void replaceTwinClassFieldForTwinsOfClass(UUID twinClassId, UUID fromTwinClassFieldId, UUID toTwinClassFieldId) {
        //nothing to replace
    }
}
