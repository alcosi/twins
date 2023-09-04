package org.twins.core.dao.domain;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DomainBusinessAccountRepository extends CrudRepository<DomainBusinessAccountEntity, UUID>, JpaSpecificationExecutor<DomainBusinessAccountEntity> {
    DomainBusinessAccountEntity findByDomainIdAndBusinessAccountId(UUID domainId, UUID businessAccountId);
}
