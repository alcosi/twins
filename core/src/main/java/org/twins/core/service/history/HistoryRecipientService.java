package org.twins.core.service.history;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.notification.*;

import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class HistoryRecipientService extends EntitySecureFindServiceImpl<HistoryNotificationContextRecipientEntity> {

    private final HistoryNotificationContextRecipientRepository repository;
    private final HistoryNotificationRecipientCollectorRepository historyNotificationRecipientCollectorRepository;

    @Override
    public CrudRepository<HistoryNotificationContextRecipientEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<HistoryNotificationContextRecipientEntity, UUID> entityGetIdFunction() {
        return HistoryNotificationContextRecipientEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(HistoryNotificationContextRecipientEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(HistoryNotificationContextRecipientEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    @Transactional(readOnly = true)
    public Set<HistoryNotificationRecipientCollectorEntity> getRecipientCollectors(UUID recipientId) {
        //todo perhaps this can be cached
        return historyNotificationRecipientCollectorRepository.findAllByHistoryNotificationContextRecipientId(recipientId);
    }
}
