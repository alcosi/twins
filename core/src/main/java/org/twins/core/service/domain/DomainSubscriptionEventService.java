package org.twins.core.service.domain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.cambium.featurer.dao.FeaturerRepository;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.domain.DomainRepository;
import org.twins.core.dao.domain.DomainSubscriptionEventEntity;
import org.twins.core.dao.domain.DomainSubscriptionEventRepository;
import org.twins.core.dao.domain.SubscriptionEventType;

import java.util.Arrays;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class DomainSubscriptionEventService extends EntitySecureFindServiceImpl<DomainSubscriptionEventEntity> {

    private final DomainSubscriptionEventRepository domainSubscriptionEventRepository;
    private final FeaturerService featurerService;
    private final FeaturerRepository featurerRepository;
    private final DomainRepository domainRepository;

    @Override
    public CrudRepository<DomainSubscriptionEventEntity, UUID> entityRepository() {
        return domainSubscriptionEventRepository;
    }

    @Override
    public Function<DomainSubscriptionEventEntity, UUID> entityGetIdFunction() {
        return DomainSubscriptionEventEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(DomainSubscriptionEventEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(DomainSubscriptionEventEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        switch (entityValidateMode) {
            case beforeSave -> {
                if (entity.getDomainId() == null) {
                    return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty domainId");
                }
                if (entity.getSubscriptionEventTypeId() == null) {
                    return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty subscriptionEventTypeId");
                }
                if (entity.getDispatcherFeaturerId() == null) {
                    return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty featurerId");
                }

                if (!domainRepository.existsById(entity.getDomainId())) {
                    return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " incorrect domain id");
                }

                if (!featurerRepository.existsById(entity.getDispatcherFeaturerId())) {
                    return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " incorrect featurer id");
                }

                if (Arrays.stream(SubscriptionEventType.values()).noneMatch(item -> item == entity.getSubscriptionEventTypeId())) {
                    return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " incorrect subscription event type");
                }

                if (entity.getDispatcherFeaturer() == null || entity.getDispatcherFeaturer().getId() != entity.getDispatcherFeaturerId()) {
                    try {
                        entity.setDispatcherFeaturer(featurerService.getFeaturerEntity(entity.getDispatcherFeaturerId()));
                    } catch (Exception e) {
                        return logErrorAndReturnFalse("Featurer with id[" + entity.getDispatcherFeaturerId() + "] does not exist");
                    }
                }
            }
        }

        return true;
    }

    public DomainSubscriptionEventEntity createDomainSubscriptionEvent(DomainSubscriptionEventEntity domainSubscriptionEventEntity) throws ServiceException {
        return saveSafe(domainSubscriptionEventEntity);
    }

    public DomainSubscriptionEventEntity updateDomainSubscriptionEvent(DomainSubscriptionEventEntity domainSubscriptionEventEntity) throws ServiceException {
        return saveSafe(domainSubscriptionEventEntity);
    }

    public void deleteDomainSubscriptionEvent(UUID id) throws ServiceException {
        deleteSafe(id);
    }
}
