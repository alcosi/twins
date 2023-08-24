package org.twins.core.dao.user;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserGroupMapRepository extends CrudRepository<UserGroupMapEntity, UUID>, JpaSpecificationExecutor<UserGroupMapEntity> {
}
