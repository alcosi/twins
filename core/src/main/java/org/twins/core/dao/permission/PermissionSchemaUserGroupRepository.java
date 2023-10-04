package org.twins.core.dao.permission;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PermissionSchemaUserGroupRepository extends CrudRepository<PermissionSchemaUserGroupEntity, UUID>, JpaSpecificationExecutor<PermissionSchemaUserGroupEntity> {
    List<PermissionSchemaUserGroupEntity> findByPermissionSchemaIdAndUserGroupIdIn(UUID permissionSchemaId, List<UUID> userGroupIdList);
}
