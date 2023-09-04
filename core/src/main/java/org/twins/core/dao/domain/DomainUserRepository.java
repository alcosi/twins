package org.twins.core.dao.domain;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.twins.core.dao.businessaccount.BusinessAccountUserEntity;

import java.util.UUID;

@Repository
public interface DomainUserRepository extends CrudRepository<DomainUserEntity, UUID>, JpaSpecificationExecutor<DomainUserEntity> {
    DomainUserEntity findByDomainIdAndUserId(UUID domainId, UUID userId);
}
