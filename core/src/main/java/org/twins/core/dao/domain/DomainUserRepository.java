package org.twins.core.dao.domain;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DomainUserRepository extends CrudRepository<DomainUserEntity, UUID>, JpaSpecificationExecutor<DomainUserEntity> {
    <T> T findByDomainIdAndUserId(UUID domainId, UUID userId, Class<T> type);
}
