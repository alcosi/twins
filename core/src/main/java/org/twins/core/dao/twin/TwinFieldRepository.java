package org.twins.core.dao.twin;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface TwinFieldRepository extends CrudRepository<TwinFieldEntity, UUID>, JpaSpecificationExecutor<TwinFieldEntity> {

    @Query(value = "SELECT SUM(CAST(f.value AS DECIMAL)) FROM twin_field f", nativeQuery = true)
    Long findTotalAmount();

    @Query(value = "from TwinClassFieldEntity field, TwinClassEntity  class " +
            "where field.key = :key " +
            "and field.twinClassId = class.extendsTwinClassId and class.id = :twinClassId")
    TwinClassFieldEntity findByTwinClassIdAndParentKey(@Param("twinClassId") UUID twinClassId, @Param("key") String key);

    List<TwinFieldEntity> findByTwinId(UUID twinId);

    List<TwinFieldEntity> findByTwinIdIn(Collection<UUID> twinIdList);

    TwinFieldEntity findByTwinIdAndTwinClassField_Key(UUID twinId, String key);

    TwinFieldEntity findByTwinIdAndTwinClassFieldId(UUID twinId, UUID twinClassFieldId);

    void deleteByTwinId(UUID twinId);
}
