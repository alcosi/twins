package org.twins.core.dao.twinclass;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface TwinClassSearchPredicateRepository extends CrudRepository<TwinClassSearchPredicateEntity, UUID> {
    List<TwinClassSearchPredicateEntity> findByTwinClassSearchId(UUID searchId);

}
