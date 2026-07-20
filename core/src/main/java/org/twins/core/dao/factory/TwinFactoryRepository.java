package org.twins.core.dao.factory;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TwinFactoryRepository extends CrudRepository<TwinFactoryEntity, UUID>, JpaSpecificationExecutor<TwinFactoryEntity> {
    /**
     * Uniqueness check against {@code twin_factory_domain_id_key_uindex} (domain_id, key). Used by
     * the duplicate engine to generate a non-colliding key for cascaded next/afterCommit factories
     * whose key is not supplied by the caller.
     */
    boolean existsByKeyAndDomainId(String key, UUID domainId);
}
