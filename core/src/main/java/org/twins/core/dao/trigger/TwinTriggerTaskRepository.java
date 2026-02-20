package org.twins.core.dao.trigger;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.UUID;

@Repository
public interface TwinTriggerTaskRepository extends CrudRepository<TwinTriggerTaskEntity, UUID>, JpaSpecificationExecutor<TwinTriggerTaskEntity> {
    Collection<TwinTriggerTaskEntity> findByStatusIdIn(Collection<TwinTriggerTaskStatus> statusId);

    Page<TwinTriggerTaskEntity> findByStatusIdIn(Collection<TwinTriggerTaskStatus> statusId, Pageable pageable);
}
