package org.twins.core.dao.user;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserGroupRepository extends CrudRepository<UserGroupEntity, UUID>, JpaSpecificationExecutor<UserGroupEntity> {

    @Query("select distinct ug.id from UserGroupEntity ug where ug.businessAccountId = :businessAccountId and ug.domainId = :domainId and ug.userGroupTypeId = :type")
    List<UUID> findAllByBusinessAccountIdAndDomainIdAndType(UUID businessAccountId, UUID domainId, String type);

}
