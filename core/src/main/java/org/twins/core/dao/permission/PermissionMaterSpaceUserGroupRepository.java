package org.twins.core.dao.permission;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PermissionMaterSpaceUserGroupRepository extends CrudRepository<PermissionMaterSpaceUserGroupEntity, UUID>, JpaSpecificationExecutor<PermissionMaterSpaceUserGroupEntity> {
    @Query(value = "select count(p) from PermissionMaterSpaceUserGroupEntity p where p.grantsCount < 0")
    long countInvalidGrantsCount();
}
