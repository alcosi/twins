package org.twins.core.dao.action;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface TwinActionPermissionRepository extends CrudRepository<TwinActionPermissionEntity, UUID>, JpaSpecificationExecutor<TwinActionPermissionEntity> {
    List<TwinActionPermissionEntity> findByTwinClassId(UUID twinClassId);

    List<TwinActionPermissionEntity> findByTwinClassIdIn(Set<UUID> twinClassIds);
}
