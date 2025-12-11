package org.twins.core.dao.user;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface UserGroupMapType1Repository extends CrudRepository<UserGroupMapType1Entity, UUID>, JpaSpecificationExecutor<UserGroupMapType1Entity> {
    boolean existsByUserIdAndUserGroupId(UUID userId, UUID userGroupId);
    UserGroupMapType1Entity findByUserIdAndUserGroupId(UUID userId, UUID userGroupId);
    UserGroupMapType1Entity findByUserIdAndUserGroupIdAndUserGroup_BusinessAccountIdAndUserGroup_DomainId(UUID userId, UUID userGroupId, UUID businessAccountId, UUID domainId);
    UserGroupMapType1Entity findByUserIdAndUserGroupIdAndUserGroup_BusinessAccountId(UUID userId, UUID userGroupId, UUID businessAccountId);
    UserGroupMapType1Entity findByUserIdAndUserGroupIdAndUserGroup_DomainId(UUID userId, UUID userGroupId, UUID domainId);

    List<UserGroupMapType1Entity> findByUserIdInAndUserGroup_BusinessAccountIdAndUserGroup_DomainId(Collection<UUID> userIds, UUID businessAccountId, UUID domainId);
    List<UserGroupMapType1Entity> findByUserIdInAndUserGroup_BusinessAccountId(Collection<UUID> userIds, UUID businessAccountId);
    List<UserGroupMapType1Entity> findByUserIdInAndUserGroup_DomainId(Collection<UUID> userIds, UUID domainId);

    @Query("select distinct ugm.userId from UserGroupMapType1Entity ugm where ugm.userGroupId in :userGroupIds and ugm.userGroup.domainId = :domainId")
    Set<UUID> findUserIdsByUserGroupIdsAndDomainId(UUID domainId, Collection<UUID> userGroupIds);
    @Query("select distinct ugm.userId from UserGroupMapType1Entity ugm where ugm.userGroupId in :userGroupIds and ugm.userGroup.businessAccountId = :businessAccountId")
    Set<UUID> findUserIdsByUserGroupIdsAndBusinessAccountId(UUID businessAccountId, Collection<UUID> userGroupIds);
    @Query("select distinct ugm.userId from UserGroupMapType1Entity ugm where ugm.userGroupId in :userGroupIds and ugm.userGroup.domainId = :domainId and ugm.userGroup.businessAccountId = :businessAccountId")
    Set<UUID> findUserIdsByUserGroupIdsAndDomainIdAndBusinessAccountId(UUID domainId, UUID businessAccountId, Collection<UUID> userGroupIds);
}
