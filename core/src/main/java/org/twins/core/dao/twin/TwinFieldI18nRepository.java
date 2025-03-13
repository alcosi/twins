package org.twins.core.dao.twin;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface TwinFieldI18nRepository extends CrudRepository<TwinFieldI18nEntity, UUID>, JpaSpecificationExecutor<TwinFieldI18nEntity> {
}
