package org.twins.core.dao.scheduler;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface SchedulerRepository extends JpaRepository<SchedulerEntity, UUID>, JpaSpecificationExecutor<SchedulerEntity> {
}
