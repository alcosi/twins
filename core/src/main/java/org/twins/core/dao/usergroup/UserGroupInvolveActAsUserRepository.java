package org.twins.core.dao.usergroup;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.twins.core.dao.user.UserGroupEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserGroupInvolveActAsUserRepository extends CrudRepository<UserGroupInvolveActAsUserEntity, UUID>, JpaSpecificationExecutor<UserGroupInvolveActAsUserEntity> {
    @Query(value = "select ug from UserGroupEntity ug join UserGroupInvolveActAsUserEntity ugiau on ug.id = ugiau.userGroupId where ugiau.machineUserId = :userId and ugiau.domainId = :domainId")
    List<UserGroupEntity> findByMachineUserIdAndDomainId(UUID userId, UUID domainId);
}
