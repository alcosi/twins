package org.twins.core.dao.action;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface TwinActionRepository extends CrudRepository<TwinActionEntity, UUID>, JpaSpecificationExecutor<TwinActionEntity> {
    TwinActionEntity findById(TwinAction id);
}
