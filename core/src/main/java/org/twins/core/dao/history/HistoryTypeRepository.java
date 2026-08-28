package org.twins.core.dao.history;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistoryTypeRepository extends CrudRepository<HistoryTypeEntity, String> {
}
