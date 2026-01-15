package org.twins.core.dao.twin;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.twins.core.dao.TwinChangeTaskStatus;

import java.util.List;
import java.util.UUID;

@Repository
public interface TwinChangeTaskRepository extends CrudRepository<TwinChangeTaskEntity, UUID>, JpaSpecificationExecutor<TwinChangeTaskEntity> {
    List<TwinChangeTaskEntity> findByStatusIdIn(List<TwinChangeTaskStatus> needStartStatuses);
    List<TwinChangeTaskEntity> findByStatusIdIn(List<TwinChangeTaskStatus> needStartStatuses, Pageable pageable);
}
