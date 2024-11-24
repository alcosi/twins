package org.twins.core.dao.permission;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PermissionGrantSpaceRoleRepository extends CrudRepository<PermissionGrantSpaceRoleEntity, UUID>, JpaSpecificationExecutor<PermissionGrantSpaceRoleEntity> {
    List<PermissionGrantSpaceRoleEntity> findByPermissionId(UUID permissionId);
}
