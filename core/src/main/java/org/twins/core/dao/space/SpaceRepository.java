package org.twins.core.dao.space;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.twins.core.dao.permission.PermissionSchemaUserGroupEntity;

import java.util.UUID;

@Repository
public interface SpaceRepository extends CrudRepository<SpaceEntity, UUID>, JpaSpecificationExecutor<SpaceEntity> {
}
