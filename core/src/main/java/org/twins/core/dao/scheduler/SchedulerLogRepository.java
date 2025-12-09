package org.twins.core.dao.scheduler;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface SchedulerLogRepository extends JpaRepository<SchedulerLogEntity, UUID>, JpaSpecificationExecutor<SchedulerLogEntity> {
    void deleteBatch(Pageable pageable);
}
