package org.twins.core.dao.permission;

import org.hibernate.query.TypedParameterValue;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PermissionRepository extends CrudRepository<PermissionEntity, UUID>, JpaSpecificationExecutor<PermissionEntity> {

    boolean existsByIdAndPermissionGroup_DomainId(UUID permissionId, UUID domainId);

    List<PermissionEntity> findByIdIn(Collection<UUID> ids);

    Optional<PermissionEntity> findByPermissionGroup_DomainIdAndKey(UUID uuid, String key);
}
