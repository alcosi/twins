package org.twins.core.dao.twinclass;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface TwinClassFieldSearchPredicateRepository extends CrudRepository<TwinClassFieldSearchPredicateEntity, UUID> {
    List<TwinClassFieldSearchPredicateEntity> findByTwinClassFieldSearchId(UUID searchId);
}
