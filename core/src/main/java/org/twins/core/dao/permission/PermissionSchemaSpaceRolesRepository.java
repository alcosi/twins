package org.twins.core.dao.permission;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PermissionSchemaSpaceRolesRepository extends CrudRepository<PermissionSchemaSpaceRolesEntity, UUID> {
    List<PermissionSchemaSpaceRolesEntity> findByPermissionId(UUID permissionId);
    List<PermissionSchemaSpaceRolesEntity> findByPermissionIdAndSpaceRoleId(UUID permissionId, UUID spaceRoleId);


}
