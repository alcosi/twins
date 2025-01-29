package org.twins.core.dao.domain;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface DomainTypeTwinClassOwnerTypeRepository extends CrudRepository<DomainTypeTwinClassOwnerTypeEntity, UUID> {
    List<DomainTypeTwinClassOwnerTypeEntity> findAllByDomainTypeId(DomainType domainType);
}
