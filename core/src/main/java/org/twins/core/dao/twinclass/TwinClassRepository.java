package org.twins.core.dao.twinclass;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    @Query(value = "SELECT * FROM twin_class WHERE domain_id = :domainId and extends_hierarchy_tree ~ ANY (:twinClassIds::ltree[])",
            nativeQuery = true)
    List<TwinClassEntity> findByDomainIdAndExtendsHierarchyContains(@Param("domainId") UUID domainId, @Param("twinClassIds") List<String> values);

    @Query(value = "SELECT * FROM twin_class WHERE domain_id = :domainId and twin_class.head_hierarchy_tree ~ ANY (:twinClassIds::ltree[])",
            nativeQuery = true)
    List<TwinClassEntity> findByDomainIdAndHeadHierarchyContains(@Param("domainId") UUID domainId, @Param("twinClassIds") List<String> values);

    boolean existsByDomainIdAndId(UUID domainId, UUID twinClassId);

    boolean existsByDomainIdAndKey(UUID domainId, String key);

    List<TwinClassEntity> findByIdIn(Collection<UUID> ids);
}
