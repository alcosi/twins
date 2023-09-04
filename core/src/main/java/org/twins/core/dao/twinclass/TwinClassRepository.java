package org.twins.core.dao.twinclass;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TwinClassRepository extends CrudRepository<TwinClassEntity, UUID>, JpaSpecificationExecutor<TwinClassEntity> {
    List<TwinClassEntity> findByDomainId(UUID domainId);
    List<TwinClassEntity> findByDomainIdAndIdIn(UUID domainId, List<UUID> ids);
    TwinClassEntity findByDomainIdAndId(UUID domainId, UUID id);
}
