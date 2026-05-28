package org.twins.core.dao.domain;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DomainBusinessAccountUserRepository extends CrudRepository<DomainBusinessAccountUserEntity, DomainBusinessAccountUserEntity.Pk>, JpaSpecificationExecutor<DomainBusinessAccountUserEntity> {
    List<DomainBusinessAccountUserEntity> findByDomainIdAndUserId(UUID domainId, UUID userId);

    List<DomainBusinessAccountUserEntity> findByUserId(UUID userId);

    List<DomainBusinessAccountUserEntity> findByDomainId(UUID domainId);

    List<DomainBusinessAccountUserEntity> findByBusinessAccountIdAndUserId(UUID businessAccountId, UUID userId);

    DomainBusinessAccountUserEntity findByDomainIdAndBusinessAccountIdAndUserId(UUID domainId, UUID businessAccountId, UUID userId);

    @Modifying
    @Query("UPDATE DomainBusinessAccountUserEntity e SET e.lastActivityAt = CURRENT_TIMESTAMP "
         + "WHERE e.domainId = :domainId AND e.businessAccountId = :businessAccountId AND e.userId = :userId")
    void updateLastActivityAt(@Param("domainId") UUID domainId, @Param("businessAccountId") UUID businessAccountId, @Param("userId") UUID userId);
}
