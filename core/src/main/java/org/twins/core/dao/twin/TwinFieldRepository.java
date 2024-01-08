package org.twins.core.dao.twin;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface TwinFieldRepository extends CrudRepository<TwinFieldEntity, UUID>, JpaSpecificationExecutor<TwinFieldEntity> {
    List<TwinFieldEntity> findByTwinId(UUID twinId);

    List<TwinFieldEntity> findByTwinIdIn(Collection<UUID> twinIdList);

    TwinFieldEntity findByTwinIdAndTwinClassField_Key(UUID twinId, String key);

    TwinFieldEntity findByTwinIdAndTwinClassFieldId(UUID twinId, UUID twinClassFieldId);

    void deleteByTwinId(UUID twinId);
}
