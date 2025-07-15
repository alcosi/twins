package org.twins.core.featurer.fieldtyper.storage;

import lombok.RequiredArgsConstructor;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.util.KitUtils;
import org.springframework.stereotype.Component;
import org.twins.core.dao.space.SpaceRoleUserEntity;
import org.twins.core.dao.space.SpaceRoleUserRepository;
import org.twins.core.dao.twin.TwinEntity;

import java.util.*;

@Component
@RequiredArgsConstructor
public class TwinFieldStorageSpaceRoleUser implements TwinFieldStorage {
    private final SpaceRoleUserRepository spaceRoleUserRepository;

    @Override
    public void load(Kit<TwinEntity, UUID> twinsKit) {
        Set<UUID> spaceSet = new HashSet<>();
        for (var twin : twinsKit) {
            spaceSet.add(twin.getPermissionSchemaSpaceId());
        }
        KitGrouped<SpaceRoleUserEntity, UUID, UUID> allTwinsFieldGrouped = new KitGrouped<>(
                spaceRoleUserRepository.findByTwinIdIn(spaceSet), SpaceRoleUserEntity::getId, SpaceRoleUserEntity::getTwinId);
        if (!KitUtils.isEmpty(allTwinsFieldGrouped)) {
            for (var twinEntity : twinsKit) {
                twinEntity.setTwinFieldSpaceUserKit(new KitGrouped<>(
                        allTwinsFieldGrouped.getGrouped(twinEntity.getPermissionSchemaSpaceId()),
                        SpaceRoleUserEntity::getId,
                        SpaceRoleUserEntity::getSpaceRoleId));
            }
        }
    }

    @Override
    public boolean hasStrictValues(UUID twinClassFieldId) {
        return false;
    }

    @Override
    public boolean isLoaded(TwinEntity twinEntity) {
        return twinEntity.getTwinFieldSpaceUserKit() != null;
    }

    @Override
    public void initEmpty(TwinEntity twinEntity) {
        twinEntity.setTwinFieldSpaceUserKit(KitGrouped.EMPTY);
    }

    @Override
    public Collection<UUID> findUsedFields(UUID twinClassId, Set<UUID> twinClassFieldIdSet) {
        return Collections.EMPTY_LIST;
    }
}
