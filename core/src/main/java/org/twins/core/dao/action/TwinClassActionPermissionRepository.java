package org.twins.core.dao.action;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface TwinClassActionPermissionRepository extends CrudRepository<TwinClassActionPermissionEntity, UUID>, JpaSpecificationExecutor<TwinClassActionPermissionEntity> {
    List<TwinClassActionPermissionEntity> findByTwinClassId(UUID twinClassId);

    List<TwinClassActionPermissionEntity> findByTwinClassIdIn(Set<UUID> twinClassIds);
}
