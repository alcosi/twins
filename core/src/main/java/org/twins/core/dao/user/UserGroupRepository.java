package org.twins.core.dao.user;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserGroupRepository extends CrudRepository<UserGroupEntity, UUID>, JpaSpecificationExecutor<UserGroupEntity> {

    // TODO: fix this query
    @Query("select t from UserGroupEntity t where t.businessAccountId = :businessAccountId")
    void deleteAllByBusinessAccountIdAndDomainId(UUID businessAccountId, UUID domainId);

}
