package org.twins.core.dao.statistic;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TwinStatisticRepository extends CrudRepository<TwinStatisticEntity, UUID> {
}
