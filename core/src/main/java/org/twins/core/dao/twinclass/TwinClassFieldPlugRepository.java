package org.twins.core.dao.twinclass;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface TwinClassFieldPlugRepository extends CrudRepository<TwinClassFieldPlugEntity, UUID>, JpaSpecificationExecutor<TwinClassFieldPlugEntity> {

    List<TwinClassFieldPlugEntity> findByTwinClassIdIn(Collection<UUID> twinClassIds);
    TwinClassFieldPlugEntity findByTwinClassIdAndTwinClassFieldId(UUID twinClassId, UUID twinClassFieldId);
    boolean existsByTwinClassIdAndTwinClassFieldId(UUID twinClassId, UUID twinClassFieldId);
}
