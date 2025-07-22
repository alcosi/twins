package org.twins.core.dao.twinclass;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface TwinClassFieldSearchRepository extends CrudRepository<TwinClassFieldSearchEntity, UUID> {
    @Override
    Optional<TwinClassFieldSearchEntity> findById(UUID uuid);
}
