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
public interface UserGroupMapType2Repository extends CrudRepository<UserGroupMapType2Entity, UUID>, JpaSpecificationExecutor<UserGroupMapType2Entity> {
    @Query("select ugm.id from UserGroupMapType2Entity ugm where ugm.businessAccountId = :businessAccountId and ugm.userGroup.domainId = :domainId and ugm.userGroup.userGroupTypeId = :type")
    List<UUID> findAllByBusinessAccountIdAndDomainIdAndType(UUID businessAccountId, UUID domainId, String type);

    UserGroupMapType2Entity findByUserIdAndUserGroupIdAndBusinessAccountId(UUID userId, UUID userGroupId, UUID businessAccountId);
    List<UserGroupMapType2Entity> findByUserIdInAndBusinessAccountId(Collection<UUID> userIds, UUID businessAccountId);

    boolean existsByUserIdAndUserGroupIdAndBusinessAccountId(UUID userId, UUID userGroupId, UUID businessAccountId);

    @Query("select distinct ugm.userId from UserGroupMapType2Entity ugm where ugm.businessAccountId = :businessAccountId and ugm.userGroupId in :groupIds")
    Set<UUID> findUserIdsByBusinessAccountIdAndUserGroupIds(UUID businessAccountId, Collection<UUID> groupIds);
}
