package org.twins.core.service.notification;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dao.notification.NotificationContextCollectorEntity;
import org.twins.core.dao.notification.NotificationContextCollectorRepository;
import org.twins.core.dao.notification.NotificationContextEntity;
import org.twins.core.dao.notification.NotificationContextRepository;
import org.twins.core.featurer.notificator.context.ContextCollector;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class NotificationContextService extends EntitySecureFindServiceImpl<NotificationContextEntity> {
    private final FeaturerService featurerService;
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

    public Set<NotificationContextCollectorEntity> getContextCollectors(UUID contextId) {
        //todo perhaps this can be cached
        return notificationContextCollectorRepository.findAllByNotificationContextId(contextId);
    }

    public Map<String, String> collectHistoryContext(UUID contextId, HistoryEntity history) throws ServiceException {
        Map<String, String> context = new HashMap<>();
        for (NotificationContextCollectorEntity contextCollector : getContextCollectors(contextId)) {
            ContextCollector collector = featurerService.getFeaturer(contextCollector.getContextCollectorFeaturerId(), ContextCollector.class);
            context = collector.collectData(history, context, contextCollector.getContextCollectorParams());
        }
        return context;
    }
}
