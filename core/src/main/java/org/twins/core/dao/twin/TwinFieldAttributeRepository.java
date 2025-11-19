package org.twins.core.dao.twin;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface TwinFieldAttributeRepository extends CrudRepository<TwinFieldAttributeEntity, UUID>, JpaSpecificationExecutor<TwinFieldAttributeEntity> {
    List<TwinFieldAttributeEntity> findByTwinClassFieldIdIn(Set<UUID> twinClassFieldIds);
    List<TwinFieldAttributeEntity> findByTwinIdIn(Set<UUID> twinIds);
    List<TwinFieldAttributeEntity> findByIdIn(Collection<UUID> ids);
}
