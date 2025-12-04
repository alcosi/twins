package org.twins.core.dao.scheduler;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SchedulerLogRepository extends JpaRepository<SchedulerLogEntity, UUID> {
}
