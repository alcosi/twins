package org.twins.core.dao.user;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface UserRepository extends CrudRepository<UserEntity, UUID>, JpaSpecificationExecutor<UserEntity> {
    @Query(value = "select businessAccountUser.userId from BusinessAccountUserEntity businessAccountUser where businessAccountUser.businessAccountId = :businessAccountId")
    List<UUID> findUserIdByBusinessAccountId(@Param("businessAccountId") UUID businessAccountId);

    @Query(value = "select domainUser.userId from DomainUserEntity domainUser where domainUser.domainId = :domainId")
    List<UUID> findUserIdByDomainId(@Param("domainId") UUID domainId);

    @Query(value = "select count(domainUser.userId) from DomainUserEntity domainUser where domainUser.domainId = :domainId")
    long countByDomainId(@Param("domainId") UUID domainId);

    @Query(value = "select user from DomainUserEntity domainUser join UserEntity user on domainUser.userId = user.id where domainUser.domainId = :domainId")
    List<UserEntity> findByDomainId(@Param("domainId") UUID domainId);

    @Query(value = "select domainUser.userId from DomainUserEntity domainUser join BusinessAccountUserEntity businessAccountUser on domainUser.userId = businessAccountUser.userId " +
            "where businessAccountUser.businessAccountId = :businessAccountId " +
            "and domainUser.domainId = :domainId")
    List<UUID> findUserIdByBusinessAccountIdAndDomainId(@Param("businessAccountId") UUID businessAccountId, @Param("domainId") UUID domainId);

    @Query(value = "select domainUser.userId from DomainUserEntity domainUser join BusinessAccountUserEntity businessAccountUser on domainUser.userId = businessAccountUser.userId " +
            "where businessAccountUser.businessAccountId = :businessAccountId " +
            "and domainUser.domainId = :domainId " +
            "and domainUser.userId in :userIds")
    Set<UUID> findUserIdByBusinessAccountIdAndDomainIdFiltered(@Param("businessAccountId") UUID businessAccountId, @Param("domainId") UUID domainId, @Param("userIds") Collection<UUID> userIds);

    @Query(value = "select user from UserEntity user join DomainUserEntity domainUser on user.id = domainUser.userId join BusinessAccountUserEntity businessAccountUser on user.id = businessAccountUser.userId " +
            "where businessAccountUser.businessAccountId = :businessAccountId " +
            "and domainUser.domainId = :domainId " +
            "and user.id = :userId")
    UserEntity findUserByUserIdAndBusinessAccountIdAndDomainId(@Param("userId") UUID userId, @Param("businessAccountId") UUID businessAccountId, @Param("domainId") UUID domainId);

    @Query(value = "select exists(" +
            "select 1 from BusinessAccountUserEntity ba_user " +
            "join DomainUserEntity d_user on d_user.userId = :userId " +
            "where ba_user.businessAccountId = :businessAccountId " +
            "and d_user.domainId = :domainId " +
            "and ba_user.userId = :userId)")
    boolean existsUserInBusinessAccountAndInDomain(@Param("userId") UUID userId, @Param("businessAccountId") UUID businessAccountId, @Param("domainId") UUID domainId);

    @Query(value = "select exists(" +
            "select 1 from BusinessAccountUserEntity ba_user " +
            "where ba_user.businessAccountId = :businessAccountId " +
            "and ba_user.userId = :userId)")
    boolean existsUserInBusinessAccount(@Param("userId") UUID userId, @Param("businessAccountId") UUID businessAccountId);

    @Query(value = "select exists(" +
            "select 1 from DomainUserEntity d_user " +
            "where d_user.domainId = :domainId " +
            "and d_user.userId = :userId)")
    boolean existsUserInDomain(@Param("userId") UUID userId, @Param("domainId") UUID domainId);


    @Query(value = "select dba.domain, dba.businessAccount, user from UserEntity user " +
            "join DomainUserEntity domainUser on user.id = domainUser.userId " +
            "join BusinessAccountUserEntity businessAccountUser on user.id = businessAccountUser.userId " +
            "join DomainBusinessAccountEntity dba on dba.domainId = domainUser.domainId and dba.businessAccountId = businessAccountUser.businessAccountId " +
            "where businessAccountUser.businessAccountId = :businessAccountId " +
            "and domainUser.domainId = :domainId " +
            "and user.id = :userId")
    List<Object[]> findDBU_ByUserIdAndBusinessAccountIdAndDomainId(@Param("userId") UUID userId, @Param("businessAccountId") UUID businessAccountId, @Param("domainId") UUID domainId);


    @Query(value = "select user from UserEntity user join DomainUserEntity domainUser on user.id = domainUser.userId " +
            "where domainUser.domainId = :domainId " +
            "and user.id = :userId")
    UserEntity findUserByUserIdAndDomainId(@Param("userId") UUID userId, @Param("domainId") UUID domainId);

    @Query(value = "select user from UserEntity user join BusinessAccountUserEntity businessAccountUser on user.id = businessAccountUser.userId " +
            "where businessAccountUser.businessAccountId = :businessAccountId " +
            "and user.id = :userId")
    UserEntity findUserByUserIdAndBusinessAccountId(@Param("userId") UUID userId, @Param("businessAccountId") UUID businessAccountId);

    @Query(value = "select user from DomainUserEntity domainUser join BusinessAccountUserEntity businessAccountUser on domainUser.userId = businessAccountUser.userId " +
            "join UserEntity user on domainUser.userId = user.id " +
            "where businessAccountUser.businessAccountId = :businessAccountId " +
            "and domainUser.domainId = :domainId")
    List<UserEntity> findByBusinessAccountIdAndDomainId(@Param("businessAccountId") UUID businessAccountId, @Param("domainId") UUID domainId);

    @Query(value = "select count(domainUser.userId) from DomainUserEntity domainUser join BusinessAccountUserEntity businessAccountUser on domainUser.userId = businessAccountUser.userId " +
            "where businessAccountUser.businessAccountId = :businessAccountId " +
            "and domainUser.domainId = :domainId")
    long countByBusinessAccountIdAndDomainId(@Param("businessAccountId") UUID businessAccountId, @Param("domainId") UUID domainId);

    List<UserEntity> findByIdIn(Collection<UUID> idList);

    UserEntity findByEmail(String email);

    @Query(value = "select u.id from UserEntity u " +
            "left join BusinessAccountUserEntity bau on u.id = bau.userId and bau.businessAccountId = :businessAccountId " +
            "left join DomainUserEntity du on u.id = du.userId and du.domainId = :domainId " +
            "where u.id in :userIds and (bau.userId is null or du.userId is null)")
    List<UUID> getUsersOutOfDomainAndBusinessAccount(@Param("userIds") Set<UUID> userIds, @Param("businessAccountId") UUID businessAccountId, @Param("domainId") UUID domainId);
}
