package org.twins.core.dao.twinclass;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.twins.core.enums.twinclass.TwinClassFieldVisibility;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface TwinClassFieldRepository extends CrudRepository<TwinClassFieldEntity, UUID>, JpaSpecificationExecutor<TwinClassFieldEntity> {

    List<TwinClassFieldEntity> findByTwinClassId(UUID twinClassId);

    String CACHE_SYSTEM_TWIN_CLASS_FIELDS = "TwinClassFieldRepository.findBaseFieldByIdIn";
    @Cacheable(value = CACHE_SYSTEM_TWIN_CLASS_FIELDS)
    @Query("select f from TwinClassFieldEntity f where f.twinClassId = :twinClassId")
    List<TwinClassFieldEntity> findBaseFields(@Param("twinClassId") UUID twinClassId);

    @Query(value = "select field from TwinClassFieldEntity field where field.twinClassId = :twinClassId and field.fieldTyperFeaturerId in (:fieldTyperIds) and cast(field.fieldTyperParams as string) like :params")
    TwinClassFieldEntity findByTwinClassIdAndFieldTyperIdInAndFieldTyperParamsLike(@Param("twinClassId") UUID twinClassId, @Param("fieldTyperIds") Collection<Integer> fieldTyperIds, @Param("params") String params);

    String CACHE_TWIN_CLASS_FIELD_BY_TWIN_CLASS_ID_IN = "TwinClassFieldRepository.findByTwinClassIdIn";
    @Cacheable(value = CACHE_TWIN_CLASS_FIELD_BY_TWIN_CLASS_ID_IN, key = "T(org.cambium.common.util.CollectionUtils).generateUniqueKey(#twinClassIdList)")
    List<TwinClassFieldEntity> findByTwinClassIdIn(Set<UUID> twinClassIdList);

    String CACHE_TWIN_CLASS_FIELD_BY_TWIN_CLASS_ID_IN_AND_VISIBILITY = "TwinClassFieldRepository.findByTwinClassIdInAndTwinClassFieldVisibilityId";
    @Cacheable(value = CACHE_TWIN_CLASS_FIELD_BY_TWIN_CLASS_ID_IN_AND_VISIBILITY, key = "T(org.cambium.common.util.CollectionUtils).generateUniqueKey(#twinClassIdList) + '_' + #visibility")
    List<TwinClassFieldEntity> findByTwinClassIdInAndTwinClassFieldVisibilityId(Set<UUID> twinClassIdList, TwinClassFieldVisibility visibility);

    List<TwinClassFieldEntity> findByTwinClassIdOrTwinClassId(UUID twinClassId, UUID parentTwinClassId);

    String CACHE_TWIN_CLASS_FIELD_BY_TWIN_CLASS_AND_KEY = "TwinClassFieldRepository.findByTwinClassIdAndKey";
    @Cacheable(value = CACHE_TWIN_CLASS_FIELD_BY_TWIN_CLASS_AND_KEY, key = "#twinClassId + '' + #key")
    TwinClassFieldEntity findByTwinClassIdAndKey(UUID twinClassId, String key);


    String CACHE_TWIN_CLASS_FIELD_BY_KEY_AND_TWIN_CLASS_ID_IN = "TwinClassFieldRepository.findByKeyAndTwinClassIdIn";
    @Cacheable(value = CACHE_TWIN_CLASS_FIELD_BY_KEY_AND_TWIN_CLASS_ID_IN, key = "#key + '' + T(org.cambium.common.util.CollectionUtils).generateUniqueKey(#twinClassIds)")
    TwinClassFieldEntity findByKeyAndTwinClassIdIn(String key, Collection<UUID> twinClassIds);

    String CACHE_TWIN_CLASS_FIELD_BY_KEYS_AND_TWIN_CLASS_ID_IN = "TwinClassFieldRepository.findByKeyInAndTwinClassIdIn";
    @Cacheable(value = CACHE_TWIN_CLASS_FIELD_BY_KEYS_AND_TWIN_CLASS_ID_IN, key = "T(org.cambium.common.util.CollectionUtils).generateUniqueKey(#keys) + '' + T(org.cambium.common.util.CollectionUtils).generateUniqueKey(#twinClassIds)")
    List<TwinClassFieldEntity> findByKeyInAndTwinClassIdIn(Collection<String> keys, Collection<UUID> twinClassIds);

    String CACHE_TWIN_CLASS_FIELD_BY_IDS_AND_TWIN_CLASS_ID_IN = "TwinClassFieldRepository.findByIdInAndTwinClassIdIn";
    @Cacheable(value = CACHE_TWIN_CLASS_FIELD_BY_IDS_AND_TWIN_CLASS_ID_IN, key = "T(org.cambium.common.util.CollectionUtils).generateUniqueKey(#ids) + '' + T(org.cambium.common.util.CollectionUtils).generateUniqueKey(#twinClassIds)")
    List<TwinClassFieldEntity> findByIdInAndTwinClassIdIn(Collection<UUID> ids, Collection<UUID> twinClassIds);

    TwinClassFieldEntity findByTwinClass_KeyAndKey(String twinClassKey, String key);

    String CACHE_TWIN_CLASS_FIELD_BY_TWIN_CLASS_AND_PARENT_KEY = "TwinClassFieldRepository.findByTwinClassIdAndParentKey";
    @Cacheable(value = CACHE_TWIN_CLASS_FIELD_BY_TWIN_CLASS_AND_PARENT_KEY, key = "#twinClassId + '' + #key")
    @Query(value = "from TwinClassFieldEntity field, TwinClassEntity  class " +
            "where field.key = :key " +
            "and field.twinClassId = class.extendsTwinClassId and class.id = :twinClassId")
    TwinClassFieldEntity findByTwinClassIdAndParentKey(@Param("twinClassId") UUID twinClassId, @Param("key") String key);

    @Query(value = "select field.id from TwinClassFieldEntity field, TwinClassEntity twinClass where twinClass.id = :twinClassId and field.twinClassId in (twinClass.extendsHierarchyTree)")
    Set<UUID> findInheritedTwinClassFieldIds(UUID twinClassId);

    String CACHE_TWIN_CLASS_FIELD_BY_ID_IN = "TwinClassFieldRepository.findByIdIn";
    @Cacheable(value = CACHE_TWIN_CLASS_FIELD_BY_ID_IN, key = "T(org.cambium.common.util.CollectionUtils).generateUniqueKey(#ids)")
    List<TwinClassFieldEntity> findByIdIn(Collection<UUID> ids);

    boolean existsByKeyAndTwinClassId(String key, UUID twinClassId);
}
