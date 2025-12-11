package org.twins.core.dao.user;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserGroupTypeRepository extends CrudRepository<UserGroupTypeEntity, String> {
    @Override
    List<UserGroupTypeEntity> findAll();

    //todo move to specification on slugger level
    @Query("select distinct ug.userGroupType from UserGroupEntity ug where " +
            "(ug.businessAccountId is null and ug.domainId = :domainId and ug.userGroupType.sluggerFeaturerId = 2001) or " +
            "(ug.businessAccountId is null and ug.domainId = :domainId and ug.userGroupType.sluggerFeaturerId = 2002) or " +
            "(ug.businessAccountId = :businessAccountId and ug.domainId is null and ug.userGroupType.sluggerFeaturerId = 2003) or " +
            "(ug.businessAccountId = :businessAccountId and ug.domainId = :domainId and ug.userGroupType.sluggerFeaturerId = 2004) or " +
            "(ug.businessAccountId is null and ug.domainId is null and ug.userGroupType.sluggerFeaturerId = 2005)")
    List<UserGroupTypeEntity> findValidTypes(UUID domainId, UUID businessAccountId);
}
