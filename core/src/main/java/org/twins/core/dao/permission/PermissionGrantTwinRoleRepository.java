package org.twins.core.dao.permission;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PermissionGrantTwinRoleRepository extends CrudRepository<PermissionGrantTwinRoleEntity, UUID> {
    List<PermissionGrantTwinRoleEntity> findByPermissionSchemaIdAndPermissionIdAndTwinClassId(UUID permissionSchemaId, UUID permissionId, UUID twinClassId);
}
