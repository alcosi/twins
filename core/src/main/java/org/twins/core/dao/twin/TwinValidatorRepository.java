package org.twins.core.dao.twin;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TwinValidatorRepository extends CrudRepository<TwinValidatorEntity, UUID> {
}
