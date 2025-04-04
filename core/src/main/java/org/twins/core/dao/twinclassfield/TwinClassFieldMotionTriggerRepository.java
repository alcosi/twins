package org.twins.core.dao.twinclassfield;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface TwinClassFieldMotionTriggerRepository extends CrudRepository<TwinClassFieldMotionTriggerEntity, UUID>, JpaSpecificationExecutor<TwinClassFieldMotionTriggerEntity> {
    List<TwinClassFieldMotionTriggerEntity> findAllByFieldMotionIdInOrderByOrder(Collection<UUID> fieldMotionIds);
}
