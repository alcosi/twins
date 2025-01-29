package org.twins.core.dao.domain;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface DomainTypeTwinClassOwnerTypeRepository extends CrudRepository<DomainTypeTwinClassOwnerTypeEntity, UUID> {
    String CACHE_DOMAIN_OWNER_TYPE_ID = "DomainTypeTwinClassOwnerTypeRepository.findAllByDomainTypeId";

    @Cacheable(value = CACHE_DOMAIN_OWNER_TYPE_ID, key = "#domainType.id")
    List<DomainTypeTwinClassOwnerTypeEntity> findAllByDomainTypeId(DomainType domainType);
}
