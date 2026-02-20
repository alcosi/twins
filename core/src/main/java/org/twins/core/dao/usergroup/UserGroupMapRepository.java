package org.twins.core.dao.usergroup;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface UserGroupMapRepository extends CrudRepository<UserGroupMapEntity, UUID>, JpaSpecificationExecutor<UserGroupMapEntity> {
    @Query("select ugm.id from UserGroupMapEntity ugm where ugm.businessAccountId = :businessAccountId and ugm.userGroup.domainId = :domainId and ugm.userGroup.userGroupTypeId = :type")
    List<UUID> findAllByBusinessAccountIdAndDomainIdAndType(UUID businessAccountId, UUID domainId, String type);

    UserGroupMapEntity findByUserIdAndUserGroupIdAndBusinessAccountId(UUID userId, UUID userGroupId, UUID businessAccountId);

    UserGroupMapEntity findByUserIdAndUserGroupIdAndUserGroup_BusinessAccountIdAndUserGroup_DomainId(UUID userId, UUID userGroupId, UUID businessAccountId, UUID domainId);

    UserGroupMapEntity findByUserIdAndUserGroupIdAndUserGroup_BusinessAccountId(UUID userId, UUID userGroupId, UUID businessAccountId);

    UserGroupMapEntity findByUserIdAndUserGroupIdAndUserGroup_DomainId(UUID userId, UUID userGroupId, UUID domainId);

    UserGroupMapEntity findByUserIdAndUserGroupIdAndDomainId(UUID userId, UUID userGroupId, UUID domainId);

    @Query("""
                select distinct ugm.userId
                from UserGroupMapEntity ugm
                where ugm.userGroupId in :groupIds
                  and ugm.domainId = :domainId
                  and (
                        ugm.businessAccountId is null
                        or ugm.businessAccountId = :businessAccountId
                      )
            """)
    Set<UUID> getUsers(UUID domainId, UUID businessAccountId, Set<UUID> userGroupIds);

    @Query("""
                select ugm
                from UserGroupMapEntity ugm
                where ugm.userId in :userIdSet
                  and ugm.domainId = :domainId
                  and (
                        ugm.businessAccountId is null
                        or ugm.businessAccountId = :businessAccountId
                      )
            """)
    List<UserGroupMapEntity> getGroups(UUID domainId, UUID businessAccountId, Set<UUID> userIdSet);
}
