package org.twins.core.dao.user;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserGroupActAsUserInvolveRepository extends CrudRepository<UserGroupActAsUserInvolveEntity, UUID>, JpaSpecificationExecutor<UserGroupActAsUserInvolveEntity> {
    List<UserGroupActAsUserInvolveEntity> findByMachineUserIdAndDomainId(UUID userId, UUID domainId);
}
