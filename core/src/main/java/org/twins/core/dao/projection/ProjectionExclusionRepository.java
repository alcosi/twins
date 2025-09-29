package org.twins.core.dao.projection;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface ProjectionExclusionRepository  extends CrudRepository<ProjectionExclusionEntity, UUID>, JpaSpecificationExecutor<ProjectionExclusionEntity> {
    List<ProjectionExclusionEntity> findByTwinClassFieldIdIn(Set<UUID> twinClassFieldIds);
}
