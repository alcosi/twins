package org.twins.core.dao.twinflow;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface TwinflowFactoryRepository extends CrudRepository<TwinflowFactoryEntity, UUID>, JpaSpecificationExecutor<TwinflowFactoryEntity> {
    List<TwinflowFactoryEntity> findByTwinflowId(UUID twinflowId);
    List<TwinflowFactoryEntity> findByTwinflowIdIn(Collection<UUID> twinflowId);
}
