package org.twins.core.dao.scheduler;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SchedulerRepository extends JpaRepository<SchedulerEntity, UUID>, JpaSpecificationExecutor<SchedulerEntity> {

    List<SchedulerEntity> findAllByActiveTrue();
}
