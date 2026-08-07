package org.twins.core.service.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.notification.NotificationContextCollectorEntity;
import org.twins.core.dao.notification.NotificationContextCollectorRepository;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class NotificationContextCollectorService extends EntitySecureFindServiceImpl<NotificationContextCollectorEntity> {
    private final NotificationContextCollectorRepository repository;

    @Override
    public CrudRepository<NotificationContextCollectorEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<NotificationContextCollectorEntity, UUID> entityGetIdFunction() {
        return NotificationContextCollectorEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(NotificationContextCollectorEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(NotificationContextCollectorEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    /**
     * Notification context config is effectively static, so the collector list per contextId is cached:
     * {@code @Cacheable} on the repository query (by contextId) + GLOBAL {@code findEntitySafe} cache (by id).
     * Removes the per-(history, contextId) SQL in collectHistoryContext.
     */
    @Override
    public CacheSupportType getCacheSupportType() {
        return CacheSupportType.GLOBAL;
    }

    public List<NotificationContextCollectorEntity> findByContextId(UUID contextId) {
        return repository.findAllByNotificationContextId(contextId);
    }
}
