package org.twins.core.service.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.notification.HistoryNotificationContextCollectorEntity;
import org.twins.core.dao.notification.HistoryNotificationContextCollectorRepository;
import org.twins.core.dao.notification.HistoryNotificationContextEntity;
import org.twins.core.dao.notification.HistoryNotificationContextRepository;

import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class HistoryNotificationContextService extends EntitySecureFindServiceImpl<HistoryNotificationContextEntity> {

    private final HistoryNotificationContextRepository historyNotificationContextRepository;
    private final HistoryNotificationContextCollectorRepository historyNotificationContextCollectorRepository;

    @Override
    public CrudRepository<HistoryNotificationContextEntity, UUID> entityRepository() {
        return historyNotificationContextRepository;
    }

    @Override
    public Function<HistoryNotificationContextEntity, UUID> entityGetIdFunction() {
        return HistoryNotificationContextEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(HistoryNotificationContextEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(HistoryNotificationContextEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public Set<HistoryNotificationContextCollectorEntity> getContextCollectors(UUID contextId) {
        //todo perhaps this can be cached
        return historyNotificationContextCollectorRepository.findByHistoryNotificationContextId(contextId);
    }
}
