package org.twins.core.dao.twinclass;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface TwinClassRepository extends CrudRepository<TwinClassEntity, UUID>, JpaSpecificationExecutor<TwinClassEntity> {
    String CACHE_TWIN_CLASS_BY_ID = "TwinClassRepository.findById";

    @Override
    @Cacheable(value = CACHE_TWIN_CLASS_BY_ID, key = "#uuid")
    Optional<TwinClassEntity> findById(UUID uuid);

    Page<TwinClassEntity> findByDomainId(UUID domainId, Pageable pageable);

    Page<TwinClassEntity> findByDomainIdAndIdIn(UUID domainId, List<UUID> ids, Pageable pageable);

    TwinClassEntity findByDomainIdAndId(UUID domainId, UUID id);

    @Query(value = "select extendsTwinClassId from TwinClassEntity where id = :twinClassId")
    UUID findExtendedClassId(@Param("twinClassId") UUID twinClassId);

    @Query(value = "select id from TwinClassEntity where extendsTwinClassId = :twinClassId")
    List<UUID> findChildClassIdList(@Param("twinClassId") UUID twinClassId);


    Optional<TwinClassEntity> findByDomainIdAndKey(UUID domainId, String key);

    @Query(value = "select extendsHierarchyTree from TwinClassEntity where id = :twinClassId")
    String getExtendsHierarchyTree(@Param("twinClassId") UUID twinClassId);

    @Query(value = "SELECT * FROM twin_class_extends_hierarchy_any_of(:domainId, string_to_array(:twinClassIdsLQueryArray, ','))", nativeQuery = true)
    List<TwinClassEntity> findByDomainIdAndExtendsHierarchyContains(@Param("domainId") UUID domainId, @Param("twinClassIdsLQueryArray") String twinClassIdsLQueryArray);

    @Query(value = "SELECT * FROM twin_class_head_hierarchy_any_of(:domainId, string_to_array(:twinClassIdsLQueryArray, ','))", nativeQuery = true)
    List<TwinClassEntity> findByDomainIdAndHeadHierarchyContains(@Param("domainId") UUID domainId, @Param("twinClassIdsLQueryArray") String twinClassIdsLQueryArray);

    List<TwinClassExtendsProjection> findByDomainIdAndIdIn(UUID domainId, List<UUID> ids);

    boolean existsByDomainIdAndId(UUID domainId, UUID twinClassId);

    boolean existsByDomainIdAndKey(UUID domainId, String key);

    List<TwinClassEntity> findByIdIn(Collection<UUID> ids);

    List<TwinClassEntity> findByHeadTwinClassIdInAndSegmentTrue(Collection<UUID> ids);

    @Query(value = "select count(distinct tc.id)=?#{#twinClassIds.size()} from TwinClassEntity tc where tc.id in :twinClassIds")
    boolean existsAll(@Param("twinClassIds") Set<UUID> twinClassIds);
}
