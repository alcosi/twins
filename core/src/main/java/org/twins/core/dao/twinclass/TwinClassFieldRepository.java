package org.twins.core.dao.twinclass;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TwinClassFieldRepository extends CrudRepository<TwinClassFieldEntity, UUID>, JpaSpecificationExecutor<TwinClassFieldEntity> {
    List<TwinClassFieldEntity> findByTwinClassId(UUID twinClassId);

    List<TwinClassFieldEntity> findByTwinClassIdOrTwinClassId(UUID twinClassId, UUID parentTwinClassId);

    TwinClassFieldEntity findByTwinClassIdAndKey(UUID twinClassId, String key);

    @Query(value = "from TwinClassFieldEntity field, TwinClassEntity  class " +
            "where field.key = :key " +
            "and field.twinClassId = class.extendsTwinClassId and class.id = :twinClassId")
    TwinClassFieldEntity findByTwinClassIdAndParentKey(@Param("twinClassId") UUID twinClassId, @Param("key") String key);
}
