package org.twins.core.dao.permission;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface PermissionGrantUserGroupRepository extends CrudRepository<PermissionGrantUserGroupEntity, UUID>, JpaSpecificationExecutor<PermissionGrantUserGroupEntity> {
    List<PermissionGrantUserGroupEntity> findByPermissionSchemaIdAndUserGroupIdIn(UUID permissionSchemaId, List<UUID> userGroupIdList);

    List<PermissionGrantUserGroupEntity> findByPermissionSchemaIdAndPermissionIdAndUserGroupIdIn(UUID permissionSchemaId, UUID permissionIdm, Collection<UUID> userGroupIdList);
}
