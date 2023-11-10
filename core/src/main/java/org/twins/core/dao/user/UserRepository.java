package org.twins.core.dao.user;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRepository extends CrudRepository<UserEntity, UUID>, JpaSpecificationExecutor<UserEntity> {
    @Query(value = "select businessAccountUser.userId from BusinessAccountUserEntity businessAccountUser where businessAccountUser.businessAccountId = :businessAccountId")
    List<UUID> findUserIdByBusinessAccountId(@Param("businessAccountId") UUID businessAccountId);

    @Query(value = "select domainUser.userId from DomainUserEntity domainUser where domainUser.domainId = :domainId")
    List<UUID> findUserIdByDomainId(@Param("domainId") UUID domainId);
    @Query(value = "select domainUser.userId from DomainUserEntity domainUser join BusinessAccountUserEntity businessAccountUser on domainUser.userId = businessAccountUser.userId " +
            "where businessAccountUser.businessAccountId = :businessAccountId " +
            "and domainUser.domainId = :domainId")
    List<UUID> findUserIdByBusinessAccountIdAndDomainId(@Param("businessAccountId") UUID businessAccountId, @Param("domainId") UUID domainId);
}
