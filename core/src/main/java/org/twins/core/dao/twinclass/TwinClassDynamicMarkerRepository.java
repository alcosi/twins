package org.twins.core.dao.twinclass;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface TwinClassDynamicMarkerRepository extends CrudRepository<TwinClassDynamicMarkerEntity, UUID>, JpaSpecificationExecutor<TwinClassDynamicMarkerEntity> {
    String CACHE_TWIN_CLASS_DYNAMIC_MARKER_BY_TWIN_CLASS_ID_IN_INHERITABLE = "TwinClassDynamicMarkerRepository.findByTwinClassIdInInheritable";
    @Cacheable(value = CACHE_TWIN_CLASS_DYNAMIC_MARKER_BY_TWIN_CLASS_ID_IN_INHERITABLE, key = "T(org.cambium.common.util.CollectionUtils).generateUniqueKey(#mainTwinClassIdList, #extendsTwinClassIdList)")
    @Query(value = "from TwinClassDynamicMarkerEntity where twinClassId in :mainTwinClassIdList or (twinClassId in :extendsTwinClassIdList and inheritable)")
    List<TwinClassDynamicMarkerEntity> findByTwinClassIdIn(@Param("mainTwinClassIdList") Set<UUID> mainTwinClassIdList, @Param("extendsTwinClassIdList") Set<UUID> extendsTwinClassIdList);
}
