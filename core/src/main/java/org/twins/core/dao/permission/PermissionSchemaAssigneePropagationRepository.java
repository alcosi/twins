package org.twins.core.dao.permission;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PermissionSchemaAssigneePropagationRepository extends CrudRepository<PermissionSchemaAssigneePropagationEntity, UUID> {
    List<PermissionSchemaAssigneePropagationEntity> findAllByPermissionSchemaIdAndPermissionId(UUID permissionSchemaId, UUID permissionId);
}
