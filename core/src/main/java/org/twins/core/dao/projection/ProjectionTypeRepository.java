package org.twins.core.dao.projection;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface ProjectionTypeRepository extends CrudRepository<ProjectionTypeEntity, UUID>, JpaSpecificationExecutor<ProjectionTypeEntity> {
    String CACHE_PROJECTION_TYPE_BY_GROUPS = "ProjectionTypeRepository.findByGroups";

    @Cacheable(value = CACHE_PROJECTION_TYPE_BY_GROUPS, key = "#groupIds")
    @Query("SELECT p FROM ProjectionTypeEntity p WHERE p.projectionTypeGroupId IN :groupIds")
    List<ProjectionTypeEntity> findByProjectionTypeGroupIdIn(@Param("groupIds") Set<UUID> groupIds);
}
