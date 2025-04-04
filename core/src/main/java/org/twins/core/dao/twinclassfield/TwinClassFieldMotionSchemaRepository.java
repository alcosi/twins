package org.twins.core.dao.twinclassfield;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TwinClassFieldMotionSchemaRepository extends CrudRepository<TwinClassFieldMotionSchemaEntity, UUID>, JpaSpecificationExecutor<TwinClassFieldMotionSchemaEntity> {
}
