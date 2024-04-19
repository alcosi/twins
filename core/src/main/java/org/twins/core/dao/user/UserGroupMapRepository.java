package org.twins.core.dao.user;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserGroupMapRepository extends CrudRepository<UserGroupMapEntity, UUID>, JpaSpecificationExecutor<UserGroupMapEntity> {
    List<UserGroupMapEntity> findByUserIdAndUserGroup_DomainId(UUID userId, UUID domainId);
    @Query("select ugm from UserGroupMapEntity ugm where (ugm.businessAccountId = :businessAccountId or ugm.businessAccountId is null) " +
            "and ugm.userGroup.domainId = :domainId and ugm.userId = :userId")
    List<UserGroupMapEntity> findByUserIdAndBusinessAccountSafe(@Param("userId") UUID userId, @Param("domainId") UUID domainId, @Param("businessAccountId") UUID businessAccountId);
    UserGroupMapEntity findByUserIdAndUserGroupId(UUID userId, UUID userGroupId);
    int countByUserIdAndUserGroupIdNotIn(UUID userId, List<UUID> userGroupIdList);

    void deleteByUserIdAndUserGroupIdIn(UUID userId, List<UUID> userGroupIdList);

    @Query("select ugm.id from UserGroupMapEntity ugm where ugm.businessAccountId = :businessAccountId and ugm.userGroup.domainId = :domainId and ugm.userGroup.userGroupTypeId in (:types)")
    List<UUID> findAllByBusinessAccountIdAndDomainIdAndTypes(UUID businessAccountId, UUID domainId, List<String> types);

    @Query("select ugm.id from UserGroupMapEntity ugm where ugm.businessAccountId is null and ugm.userGroup.domainId = :domainId and ugm.userGroup.businessAccountId = :businessAccountId and ugm.userGroup.userGroupTypeId in (:types)")
    List<UUID> findAllByDomainIdAndTypesAndUserGroupBusinessAccount(UUID businessAccountId, UUID domainId, List<String> types);
}
