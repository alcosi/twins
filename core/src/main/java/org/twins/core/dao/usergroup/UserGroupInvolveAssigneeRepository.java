package org.twins.core.dao.usergroup;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserGroupInvolveAssigneeRepository extends CrudRepository<UserGroupInvolveAssigneeEntity, UUID>, JpaSpecificationExecutor<UserGroupInvolveAssigneeEntity> {
}
