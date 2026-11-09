package org.twins.core.dao.context;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface ContextCollectorRepository extends CrudRepository<ContextCollectorEntity, UUID>, JpaSpecificationExecutor<ContextCollectorEntity> {

    String CACHE_CONTEXT_COLLECTORS_BY_CONTEXT_ID = "ContextCollectorRepository.findAllByContextId";

    @Cacheable(value = CACHE_CONTEXT_COLLECTORS_BY_CONTEXT_ID, key = "#contextId")
    List<ContextCollectorEntity> findAllByContextId(UUID contextId);

    List<ContextCollectorEntity> findAllByContextIdIn(Set<UUID> contextIds);
}
