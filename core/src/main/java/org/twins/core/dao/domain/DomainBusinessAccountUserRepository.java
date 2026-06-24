package org.twins.core.dao.domain;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.twins.core.dao.EntryCount;
import org.twins.core.dao.businessaccount.BusinessAccountEntity;
import org.twins.core.dao.businessaccount.BusinessAccountUserEntity;
import org.twins.core.dao.user.UserEntity;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface DomainBusinessAccountUserRepository extends CrudRepository<DomainBusinessAccountUserEntity, DomainBusinessAccountUserEntity.Pk>, JpaSpecificationExecutor<DomainBusinessAccountUserEntity> {
    List<DomainBusinessAccountUserEntity> findByDomainIdAndUserId(UUID domainId, UUID userId);

    List<DomainBusinessAccountUserEntity> findByUserId(UUID userId);

    List<DomainBusinessAccountUserEntity> findByDomainId(UUID domainId);

    List<DomainBusinessAccountUserEntity> findByBusinessAccountIdAndUserId(UUID businessAccountId, UUID userId);

    @Query(value = "SELECT dbau, dbau.domainSpecOnly, dbau.businessAccountSpecOnly, dbau.userSpecOnly, dbau.domainBusinessAccountSpecOnly, dbau.domainUserSpecOnly, dbau.businessAccountUserSpecOnly " +
            "FROM DomainBusinessAccountUserEntity dbau where dbau.domainId = :domainId and dbau.businessAccountId = :businessAccountId and dbau.userId = :userId")
    List<Object[]> _findByDomainIdAndBusinessAccountIdAndUserId(@Param("domainId") UUID domainId, @Param("businessAccountId") UUID businessAccountId, @Param("userId") UUID userId);

    default DomainBusinessAccountUserEntity findByDomainIdAndBusinessAccountIdAndUserId(UUID domainId, UUID businessAccountId, UUID userId) {
        var results = _findByDomainIdAndBusinessAccountIdAndUserId(domainId, businessAccountId, userId);
        if (results == null)
            return null;
        var row = results.getFirst();
        var dbau = (DomainBusinessAccountUserEntity) row[0];
        dbau
                .setDomain((DomainEntity) row[1])
                .setBusinessAccount((BusinessAccountEntity) row[2])
                .setUser((UserEntity) row[3])
                .setDomainBusinessAccount((DomainBusinessAccountEntity) row[4])
                .setDomainUser((DomainUserEntity) row[5])
                .setBusinessAccountUser((BusinessAccountUserEntity) row[6]);
    }

    @Modifying
    @Query("UPDATE DomainBusinessAccountUserEntity e SET e.lastActivityAt = CURRENT_TIMESTAMP "
         + "WHERE e.domainId = :domainId AND e.businessAccountId = :businessAccountId AND e.userId = :userId")
    void updateLastActivityAt(@Param("domainId") UUID domainId, @Param("businessAccountId") UUID businessAccountId, @Param("userId") UUID userId);

    @Query(value = "SELECT dbau.business_account_id AS id, COUNT(dbau) AS count FROM domain_business_account_user dbau INNER JOIN \"user\" u ON u.id = dbau.user_id WHERE dbau.business_account_id IN :businessAccountIds AND u.user_status_id = 'ACTIVE' AND dbau.domain_id = :domainId GROUP BY dbau.business_account_id", nativeQuery = true)
    List<EntryCount> countUsersInBusinessAccounts(@Param("businessAccountIds") Collection<UUID> businessAccountIds, @Param("domainId") UUID domainId);
}
