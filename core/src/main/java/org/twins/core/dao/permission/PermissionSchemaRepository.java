package org.twins.core.dao.permission;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PermissionSchemaRepository extends CrudRepository<PermissionSchemaEntity, UUID>, JpaSpecificationExecutor<PermissionSchemaEntity> {
}
