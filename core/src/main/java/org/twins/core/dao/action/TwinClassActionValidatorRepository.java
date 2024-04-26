package org.twins.core.dao.action;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface TwinClassActionValidatorRepository extends CrudRepository<TwinClassActionValidatorEntity, UUID>, JpaSpecificationExecutor<TwinClassActionValidatorEntity> {
    List<TwinClassActionValidatorEntity> findByTwinClassIdOrderByOrder(UUID twinClassId);

    List<TwinClassActionValidatorEntity> findByTwinClassIdIn(Set<UUID> twinClassIds);
}
