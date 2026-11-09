package org.twins.core.service.context;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.context.ContextCollectorEntity;
import org.twins.core.dao.context.ContextCollectorRepository;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class ContextCollectorService extends EntitySecureFindServiceImpl<ContextCollectorEntity> {
    private final ContextCollectorRepository repository;

    @Override
    public CrudRepository<ContextCollectorEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<ContextCollectorEntity, UUID> entityGetIdFunction() {
        return ContextCollectorEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(ContextCollectorEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(ContextCollectorEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    /**
     * Context config is effectively static, so the collector list per contextId is cached:
     * {@code @Cacheable} on the repository query (by contextId) + GLOBAL {@code findEntitySafe} cache (by id).
     * Removes the per-(history, contextId) SQL in collectHistoryContext.
     */
    @Override
    public CacheSupportType getCacheSupportType() {
        return CacheSupportType.GLOBAL;
    }

    public List<ContextCollectorEntity> findByContextId(UUID contextId) {
        return repository.findAllByContextId(contextId);
    }

    public Collection<ContextCollectorEntity> findByContextIdIn(Set<UUID> contextIds) {
        if (contextIds == null || contextIds.isEmpty()) {
            return List.of();
        }
        return repository.findAllByContextIdIn(contextIds);
    }
}
