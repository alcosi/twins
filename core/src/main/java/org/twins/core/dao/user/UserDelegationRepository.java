package org.twins.core.dao.user;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserDelegationRepository extends CrudRepository<UserDelegationEntity, UUID>, JpaSpecificationExecutor<UserDelegationEntity> {
    @Query(value = "select delegatedUser from UserDelegationEntity where machineUserId = :userId and domainId = :domainId")
    UserEntity findByMachineUserIdAndDomainId(UUID userId, UUID domainId);
}
