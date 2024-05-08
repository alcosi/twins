package org.twins.core.dao.twin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TwinHeadRepository extends CrudRepository<TwinEntity, UUID>, JpaSpecificationExecutor<TwinEntity> {
    @Query(value = "select twin from TwinEntity twin join BusinessAccountUserEntity businessAccountUser on twin.id = businessAccountUser.userId where businessAccountUser.businessAccountId = :businessAccountId")
    Page<TwinEntity> findUserTwinByBusinessAccountId(@Param("businessAccountId") UUID businessAccountId, Pageable pageable);

    @Query(value = "select twin from TwinEntity twin join DomainUserEntity domainUser on twin.id = domainUser.userId where domainUser.domainId = :domainId")
    Page<TwinEntity> findUserTwinByDomainId(@Param("domainId") UUID domainId, Pageable pageable);
    @Query(value = "select twin from TwinEntity twin join DomainUserEntity domainUser on twin.id = domainUser.userId join BusinessAccountUserEntity businessAccountUser on domainUser.userId = businessAccountUser.userId " +
            "where businessAccountUser.businessAccountId = :businessAccountId " +
            "and domainUser.domainId = :domainId")
    Page<TwinEntity> findUserTwinByBusinessAccountIdAndDomainId(@Param("businessAccountId") UUID businessAccountId, @Param("domainId") UUID domainId, Pageable pageable);

    @Query(value = "select twin from TwinEntity twin join BusinessAccountUserEntity businessAccountUser on twin.id = businessAccountUser.businessAccountId  where businessAccountUser.userId = :userId")
    Page<TwinEntity> findBusinessAccountTwinByUser(@Param("userId") UUID userId, Pageable pageable);

    @Query(value = "select twin from TwinEntity twin join DomainBusinessAccountEntity domainBusinessAccount on twin.id = domainBusinessAccount.businessAccountId  where domainBusinessAccount.domainId = :domainId")
    Page<TwinEntity> findBusinessAccountTwinByDomainId(@Param("domainId") UUID domainId, Pageable pageable);
    @Query(value = "select twin from TwinEntity twin join DomainBusinessAccountEntity domainBusinessAccount on twin.id = domainBusinessAccount.businessAccountId  join BusinessAccountUserEntity businessAccountUser on domainBusinessAccount.businessAccountId = businessAccountUser.businessAccountId " +
            "and businessAccountUser.userId = :userId " +
            "and domainBusinessAccount.domainId = :domainId")
    Page<TwinEntity> findBusinessAccountTwinByUserIdAndDomainId(@Param("userId") UUID userId, @Param("domainId") UUID domainId, Pageable pageable);
}
