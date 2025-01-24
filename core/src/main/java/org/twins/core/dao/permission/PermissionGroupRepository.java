package org.twins.core.dao.permission;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PermissionGroupRepository extends CrudRepository<PermissionGroupEntity, UUID>, JpaSpecificationExecutor<PermissionGroupEntity> {
    Optional<PermissionGroupEntity> findByDomainIdAndKey(UUID domainId, String key);
}
