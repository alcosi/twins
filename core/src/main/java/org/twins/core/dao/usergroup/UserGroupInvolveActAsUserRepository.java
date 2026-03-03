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
    @Query(value = "select userGroup from UserGroupInvolveActAsUserEntity where machineUserId = :userId and domainId = :domainId")
    List<UserGroupEntity> findByMachineUserIdAndDomainId(UUID userId, UUID domainId);
}
