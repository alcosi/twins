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
public interface UserGroupMapType3Repository extends CrudRepository<UserGroupMapType3Entity, UUID>, JpaSpecificationExecutor<UserGroupMapType3Entity> {
    UserGroupMapType3Entity findByUserIdAndUserGroupIdAndDomainId(UUID userId, UUID userGroupId, UUID domainId);
    List<UserGroupMapType3Entity> findByUserIdInAndDomainId(Collection<UUID> userIds, UUID domainId);
    boolean existsByUserIdAndUserGroupIdAndDomainId(UUID userId, UUID userGroupId, UUID domainId);

    @Query("select distinct ugm.userId from UserGroupMapType3Entity ugm where ugm.domainId = :domainId and ugm.userGroupId in :groupIds")
    Set<UUID> findUserIdsByDomainIdIdAndUserGroupIds(UUID domainId, Collection<UUID> groupIds);

}
