package org.twins.core.dao.validator;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface TwinValidatorRepository extends CrudRepository<TwinValidatorEntity, UUID> {
    List<TwinValidatorEntity> findByTwinValidatorSetIdIn(Set<UUID> twinValidatorSetIds);
}
