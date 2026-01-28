package org.twins.core.dao.twinclass;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface TwinClassDynamicMarkerRepository extends CrudRepository<TwinClassDynamicMarkerEntity, UUID>, JpaSpecificationExecutor<TwinClassDynamicMarkerEntity> {
    List<TwinClassDynamicMarkerEntity> findByTwinClassIdIn(Collection<UUID> twinClassIds);
}
