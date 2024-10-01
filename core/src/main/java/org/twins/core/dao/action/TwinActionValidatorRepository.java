package org.twins.core.dao.action;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface TwinActionValidatorRepository extends CrudRepository<TwinActionValidatorEntity, UUID>, JpaSpecificationExecutor<TwinActionValidatorEntity> {
    List<TwinActionValidatorEntity> findByTwinClassIdOrderByOrder(UUID twinClassId);

    List<TwinActionValidatorEntity> findByTwinClassIdIn(Set<UUID> twinClassIds);
}
