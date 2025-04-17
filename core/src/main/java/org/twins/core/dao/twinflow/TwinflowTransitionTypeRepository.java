package org.twins.core.dao.twinflow;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TwinflowTransitionTypeRepository extends CrudRepository<TwinflowTransitionTypeEntity, String>, JpaSpecificationExecutor<TwinflowTransitionTypeEntity> {
}
