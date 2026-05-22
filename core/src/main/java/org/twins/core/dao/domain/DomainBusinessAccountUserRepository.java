package org.twins.core.dao.domain;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DomainBusinessAccountUserRepository extends CrudRepository<DomainBusinessAccountUserEntity, DomainBusinessAccountUserEntity.Pk>, JpaSpecificationExecutor<DomainBusinessAccountUserEntity> {
    List<DomainBusinessAccountUserEntity> findByDomainIdAndUserId(UUID domainId, UUID userId);

    List<DomainBusinessAccountUserEntity> findByUserId(UUID userId);

    List<DomainBusinessAccountUserEntity> findByDomainId(UUID domainId);

    List<DomainBusinessAccountUserEntity> findByBusinessAccountIdAndUserId(UUID businessAccountId, UUID userId);
}
