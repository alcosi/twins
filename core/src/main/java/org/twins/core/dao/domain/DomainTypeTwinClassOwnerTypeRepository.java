package org.twins.core.dao.domain;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.twins.core.dao.twinclass.TwinClassOwnerTypeEntity;
import org.twins.core.enums.domain.DomainType;

import java.util.Set;
import java.util.UUID;

public interface DomainTypeTwinClassOwnerTypeRepository extends CrudRepository<DomainTypeTwinClassOwnerTypeEntity, UUID> {
    String CACHE_DOMAIN_OWNER_TYPE_ID = "DomainTypeTwinClassOwnerTypeRepository.findAllTwinClassOwnerTypesByDomainTypeId";

    @Cacheable(value = CACHE_DOMAIN_OWNER_TYPE_ID, key = "#domainType.id")
    @Query("SELECT d.twinClassOwnerType FROM DomainTypeTwinClassOwnerTypeEntity d WHERE d.domainTypeId = :domainType")
    Set<TwinClassOwnerTypeEntity> findAllTwinClassOwnerTypesByDomainTypeId(@Param("domainType") DomainType domainType);
}
