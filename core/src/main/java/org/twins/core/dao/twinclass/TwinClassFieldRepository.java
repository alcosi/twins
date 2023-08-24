package org.twins.core.dao.twinclass;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TwinClassFieldRepository extends CrudRepository<TwinClassFieldEntity, UUID>, JpaSpecificationExecutor<TwinClassFieldEntity> {
    List<TwinClassFieldEntity> findByTwinClassId(UUID twinClassId);
}
