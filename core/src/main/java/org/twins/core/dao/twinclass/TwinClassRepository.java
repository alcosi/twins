package org.twins.core.dao.twinclass;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TwinClassRepository extends CrudRepository<TwinClassEntity, UUID>, JpaSpecificationExecutor<TwinClassEntity> {
    @Override
    @Cacheable(value = "TwinClassRepository.findById", key = "{#uuid}")
    Optional<TwinClassEntity> findById(UUID uuid);
    Page<TwinClassEntity> findByDomainId(UUID domainId, Pageable pageable);
    Page<TwinClassEntity> findByDomainIdAndIdIn(UUID domainId, List<UUID> ids, Pageable pageable);
    TwinClassEntity findByDomainIdAndId(UUID domainId, UUID id);

    @Query(value = "select extendsTwinClassId from TwinClassEntity where id = :twinClassId")
    UUID findExtendedClassId(@Param("twinClassId") UUID twinClassId);

    @Query(value = "select id from TwinClassEntity where extendsTwinClassId = :twinClassId")
    List<UUID> findChildClassIdList(@Param("twinClassId") UUID twinClassId);


    TwinClassEntity findByDomainIdAndKey(UUID domainId, String key);

    @Query(value = "select extendsHierarchyTree from TwinClassEntity where id = :twinClassId")
    String getExtendsHierarchyTree(@Param("twinClassId") UUID twinClassId);

    boolean existsByDomainIdAndId(UUID domainId, UUID twinClassId);

    boolean existsByDomainIdAndKey(UUID domainId, String key);
}
