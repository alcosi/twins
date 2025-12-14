package org.twins.core.service.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.notification.NotificationContextCollectorEntity;
import org.twins.core.dao.notification.NotificationContextCollectorRepository;
import org.twins.core.dao.notification.NotificationContextEntity;
import org.twins.core.dao.notification.NotificationContextRepository;

import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class NotificationContextService extends EntitySecureFindServiceImpl<NotificationContextEntity> {

    private final NotificationContextRepository notificationContextRepository;
    private final NotificationContextCollectorRepository notificationContextCollectorRepository;

    @Override
    public CrudRepository<NotificationContextEntity, UUID> entityRepository() {
        return notificationContextRepository;
    }

    @Override
    public Function<NotificationContextEntity, UUID> entityGetIdFunction() {
        return NotificationContextEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(NotificationContextEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(NotificationContextEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    @Transactional(readOnly = true)
    public Set<NotificationContextCollectorEntity> getContextCollectors(UUID contextId) {
        //todo perhaps this can be cached
        return notificationContextCollectorRepository.findAllByNotificationContextId(contextId);
    }
}
