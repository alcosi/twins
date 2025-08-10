package org.twins.core.featurer.fieldtyper.storage;

import lombok.RequiredArgsConstructor;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.springframework.stereotype.Component;
import org.twins.core.dao.space.SpaceRoleUserEntity;
import org.twins.core.dao.space.SpaceRoleUserRepository;
import org.twins.core.dao.twin.TwinEntity;

import java.util.*;

@Component
@RequiredArgsConstructor
public class TwinFieldStorageSpaceRoleUser extends TwinFieldStorage {
    private final SpaceRoleUserRepository spaceRoleUserRepository;

    @Override
    public void load(Kit<TwinEntity, UUID> twinsKit) {
        Set<UUID> spaceSet = new HashSet<>();
        for (var twin : twinsKit) {
            spaceSet.add(twin.getPermissionSchemaSpaceId());
        }
        KitGrouped<SpaceRoleUserEntity, UUID, UUID> allTwinsFieldGrouped = new KitGrouped<>(
                spaceRoleUserRepository.findByTwinIdIn(spaceSet), SpaceRoleUserEntity::getId, SpaceRoleUserEntity::getTwinId);
        for (var twinEntity : twinsKit) {
            if (allTwinsFieldGrouped.containsGroupedKey(twinEntity.getId())) {
                twinEntity.setTwinFieldSpaceUserKit(new KitGrouped<>(
                        allTwinsFieldGrouped.getGrouped(twinEntity.getPermissionSchemaSpaceId()),
                        SpaceRoleUserEntity::getId,
                        SpaceRoleUserEntity::getSpaceRoleId));
            } else {
                initEmpty(twinEntity);
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

    @Override
    public void replaceTwinClassFieldForTwinsOfClass(UUID twinClassId, UUID fromTwinClassFieldId, UUID toTwinClassFieldId) {
        //nothing to replace
    }

    @Override
    public void deleteTwinFieldsForTwins(Map<UUID, Set<UUID>> deleteMap) {
        //nothing to delete
    }

    @Override
    boolean canBeMerged(Object o) {
        return isSameClass(o);
    }
}
