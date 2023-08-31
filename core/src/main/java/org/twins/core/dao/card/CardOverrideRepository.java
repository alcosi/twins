package org.twins.core.dao.card;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CardOverrideRepository extends CrudRepository<CardOverrideEntity, UUID>, JpaSpecificationExecutor<CardOverrideEntity> {
}
