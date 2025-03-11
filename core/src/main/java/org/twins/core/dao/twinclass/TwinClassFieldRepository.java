package org.twins.core.dao.twinclass;

import org.springframework.cache.annotation.Cacheable;
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
    String CACHE_TWIN_CLASS_FIELD_BY_ID_IN = "TwinClassFieldRepository.findByIdIn";

    List<TwinClassFieldEntity> findByTwinClassId(UUID twinClassId);

    @Query(value = "select field from TwinClassFieldEntity field where field.twinClassId = :twinClassId and field.fieldTyperFeaturerId in (:fieldTyperIds) and cast(field.fieldTyperParams as string) like :params")
    TwinClassFieldEntity findByTwinClassIdAndFieldTyperIdInAndFieldTyperParamsLike(@Param("twinClassId") UUID twinClassId, @Param("fieldTyperIds") Collection<Integer> fieldTyperIds, @Param("params") String params);

    List<TwinClassFieldEntity> findByTwinClassIdIn(Set<UUID> twinClassIdList);

    List<TwinClassFieldEntity> findByTwinClassIdOrTwinClassId(UUID twinClassId, UUID parentTwinClassId);

    TwinClassFieldEntity findByTwinClassIdAndKey(UUID twinClassId, String key);

    TwinClassFieldEntity findByKeyAndTwinClassIdIn(String key, Collection<UUID> twinClassIds);

    TwinClassFieldEntity findByTwinClass_KeyAndKey(String twinClassKey, String key);

    @Query(value = "from TwinClassFieldEntity field, TwinClassEntity  class " +
            "where field.key = :key " +
            "and field.twinClassId = class.extendsTwinClassId and class.id = :twinClassId")
    TwinClassFieldEntity findByTwinClassIdAndParentKey(@Param("twinClassId") UUID twinClassId, @Param("key") String key);

    @Query(value = "select field.id from TwinClassFieldEntity field, TwinClassEntity twinClass where twinClass.id = :twinClassId and field.twinClassId in (twinClass.extendsHierarchyTree)")
    Set<UUID> findInheritedTwinClassFieldIds(UUID twinClassId);

    @Cacheable(value = CACHE_TWIN_CLASS_FIELD_BY_ID_IN, key = "T(org.cambium.common.util.CollectionUtils).generateUniqueKey(#ids)")
    List<TwinClassFieldEntity> findByIdIn(Collection<UUID> ids);

    boolean existsByKeyAndTwinClassId(String key, UUID twinClassId);
}
