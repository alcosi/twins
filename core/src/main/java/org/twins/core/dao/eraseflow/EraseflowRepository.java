package org.twins.core.dao.eraseflow;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EraseflowRepository extends CrudRepository<EraseflowEntity, UUID>, JpaSpecificationExecutor<EraseflowEntity> {
}
