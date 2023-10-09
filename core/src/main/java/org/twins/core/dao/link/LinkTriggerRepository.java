package org.twins.core.dao.link;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LinkTriggerRepository extends CrudRepository<LinkTriggerEntity, UUID>, JpaSpecificationExecutor<LinkTriggerEntity> {
}
