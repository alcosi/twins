package org.twins.core.dao.scheduler;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SchedulerRepository extends JpaRepository<SchedulerEntity, UUID> {
}
