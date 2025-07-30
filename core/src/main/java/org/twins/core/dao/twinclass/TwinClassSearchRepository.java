package org.twins.core.dao.twinclass;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface TwinClassSearchRepository extends CrudRepository<TwinClassSearchEntity, UUID> {
}
