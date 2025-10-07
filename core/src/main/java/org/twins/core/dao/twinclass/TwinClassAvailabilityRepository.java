package org.twins.core.dao.twinclass;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface TwinClassAvailabilityRepository extends CrudRepository<TwinClassAvailabilityEntity, UUID>, JpaSpecificationExecutor<TwinClassEntity> {

    List<TwinClassAvailabilityEntity> findAllByIdIn(Collection<UUID> ids);

}
