package org.twins.core.dao.permission;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface PermissionGroupRepository extends CrudRepository<PermissionGroupEntity, UUID>, JpaSpecificationExecutor<PermissionGroupEntity> {
    List<PermissionGroupEntity> findAllByIdIn(Set<UUID> ids);
}
