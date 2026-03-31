package org.twins.core.dao.factory;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TwinFactoryConditionSetRepository extends CrudRepository<TwinFactoryConditionSetEntity, UUID>, JpaSpecificationExecutor<TwinFactoryConditionSetEntity> {
    String CACHE_CONDITION_SET_BY_ID = "TwinFactoryConditionSetRepository.findById";

    @Override
    @Cacheable(value = CACHE_CONDITION_SET_BY_ID, key = "#uuid")
    Optional<TwinFactoryConditionSetEntity> findById(UUID uuid);
}
