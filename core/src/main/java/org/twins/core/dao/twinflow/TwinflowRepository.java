package org.twins.core.dao.twinflow;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TwinflowRepository extends CrudRepository<TwinflowEntity, UUID>, JpaSpecificationExecutor<TwinflowEntity> {
}
