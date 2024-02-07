package org.twins.core.dao.twinclass;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface TwinClassFieldRepository extends CrudRepository<TwinClassFieldEntity, UUID>, JpaSpecificationExecutor<TwinClassFieldEntity> {
    List<TwinClassFieldEntity> findByTwinClassId(UUID twinClassId);

    @Query(value = "select field from TwinClassFieldEntity field where field.twinClassId = :twinClassId and field.fieldTyperFeaturerId in (:fieldTyperIds) and cast(field.fieldTyperParams as string) like :params")
    TwinClassFieldEntity findByTwinClassIdAndFieldTyperIdInAndFieldTyperParamsLike(@Param("twinClassId") UUID twinClassId, @Param("fieldTyperIds") Collection<Integer> fieldTyperIds, @Param("params") String params);

    List<TwinClassFieldEntity> findByTwinClassIdIn(Set<UUID> twinClassIdList);

    List<TwinClassFieldEntity> findByTwinClassIdOrTwinClassId(UUID twinClassId, UUID parentTwinClassId);

    TwinClassFieldEntity findByTwinClassIdAndKey(UUID twinClassId, String key);

    @Query(value = "from TwinClassFieldEntity field, TwinClassEntity  class " +
            "where field.key = :key " +
            "and field.twinClassId = class.extendsTwinClassId and class.id = :twinClassId")
    TwinClassFieldEntity findByTwinClassIdAndParentKey(@Param("twinClassId") UUID twinClassId, @Param("key") String key);
}
