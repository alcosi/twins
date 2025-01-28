package org.twins.core.dao.domain;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.twins.core.dao.twinclass.TwinClassOwnerTypeEntity;

import java.util.List;
import java.util.UUID;

public interface DomainTypeTwinClassOwnerTypeRepository extends CrudRepository<DomainTypeTwinClassOwnerTypeEntity, UUID> {
    @Query("SELECT dt.twinClassOwnerType FROM DomainTypeTwinClassOwnerTypeEntity dt WHERE dt.domainId = :domainId")
    List<TwinClassOwnerTypeEntity> findTwinClassOwnerTypesByDomainId(@Param("domainId") UUID domainId);
}
