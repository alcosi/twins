package org.twins.core.dao.permission;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PermissionSchemaTwinRoleRepository extends CrudRepository<PermissionSchemaTwinRoleEntity, UUID> {
    List<PermissionSchemaTwinRoleEntity> findByPermissionSchemaIdAndPermissionIdAndTwinClassId(UUID permissionSchemaId, UUID permissionId, UUID twinClassId);
}
