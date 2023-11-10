package org.twins.core.dao.businessaccount;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BusinessAccountRepository extends CrudRepository<BusinessAccountEntity, UUID>, JpaSpecificationExecutor<BusinessAccountEntity> {

    @Query(value = "select businessAccountUser.businessAccountId from BusinessAccountUserEntity businessAccountUser where businessAccountUser.userId = :userId")
    List<UUID> findBusinessAccountIdByUser(@Param("userId") UUID userId);

    @Query(value = "select domainBusinessAccount.businessAccountId from DomainBusinessAccountEntity domainBusinessAccount where domainBusinessAccount.domainId = :domainId")
    List<UUID> findBusinessAccountIdByDomainId(@Param("domainId") UUID domainId);
    @Query(value = "select domainBusinessAccount.businessAccountId from DomainBusinessAccountEntity domainBusinessAccount join BusinessAccountUserEntity businessAccountUser on domainBusinessAccount.businessAccountId = businessAccountUser.businessAccountId " +
            "and businessAccountUser.userId = :userId " +
            "and domainBusinessAccount.domainId = :domainId")
    List<UUID> findBusinessAccountIdByUserIdAndDomainId(@Param("userId") UUID userId, @Param("domainId") UUID domainId);
}
