package org.twins.core.dao.permission;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PermissionGrantAssigneePropagationRepository extends CrudRepository<PermissionGrantAssigneePropagationEntity, UUID>, JpaSpecificationExecutor<PermissionGrantAssigneePropagationEntity> {
    List<PermissionGrantAssigneePropagationEntity> findAllByPermissionSchemaIdAndPermissionId(UUID permissionSchemaId, UUID permissionId);
}
