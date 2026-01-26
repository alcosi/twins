package org.twins.core.dao.projection;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProjectionTypeGroupRepository extends CrudRepository<ProjectionTypeGroupEntity, UUID>, JpaSpecificationExecutor<ProjectionTypeGroupEntity> {
}
