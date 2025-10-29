package org.twins.core.dao.search;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface TwinSearchPredicateRepository extends CrudRepository<TwinSearchPredicateEntity, UUID>, JpaSpecificationExecutor<TwinSearchPredicateEntity> {
    List<TwinSearchPredicateEntity> findByTwinSearchId(UUID searchId);
    List<TwinSearchPredicateEntity> findByTwinSearchIdIn(Collection<UUID> searchId);

}
