package org.twins.core.dao.scheduler;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.sql.Timestamp;
import java.util.UUID;

public interface SchedulerLogRepository extends JpaRepository<SchedulerLogEntity, UUID>, JpaSpecificationExecutor<SchedulerLogEntity> {

    long countAllByCreatedAtBefore(Timestamp createdAt);
    void deleteAllByCreatedAtBefore(Timestamp createdAt);
}
