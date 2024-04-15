package org.twins.core.dao.domain;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DomainRepository extends CrudRepository<DomainEntity, UUID>, JpaSpecificationExecutor<DomainEntity> {
    DomainEntity findByKey(String key);

    <T> T findById(UUID id, Class<T> type);

    boolean existsByKey(String key);
}
