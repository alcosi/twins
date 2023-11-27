package org.twins.core.service.twinflow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.domain.DomainBusinessAccountEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twin.TwinStatusTransitionTriggerEntity;
import org.twins.core.dao.twin.TwinStatusTransitionTriggerRepository;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinflow.*;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.TwinUpdate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.transition.trigger.TransitionTrigger;
import org.twins.core.featurer.transition.validator.TransitionValidator;
import org.twins.core.service.EntitySecureFindServiceImpl;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.domain.DomainService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twin.TwinStatusService;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwinflowTransitionService extends EntitySecureFindServiceImpl<TwinflowTransitionEntity> {
    final TwinflowTransitionRepository twinflowTransitionRepository;
    final TwinflowTransitionValidatorRepository twinflowTransitionValidatorRepository;
    final TwinflowTransitionTriggerRepository twinflowTransitionTriggerRepository;
    final TwinStatusTransitionTriggerRepository twinStatusTransitionTriggerRepository;
    final TwinClassService twinClassService;
    final TwinStatusService twinStatusService;
    @Lazy
    final TwinService twinService;
    final TwinflowService twinflowService;
    @Lazy
    final FeaturerService featurerService;

    @Override
    public CrudRepository<TwinflowTransitionEntity, UUID> entityRepository() {
        return twinflowTransitionRepository;
    }

    @Override
    public boolean isEntityReadDenied(TwinflowTransitionEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinflowTransitionEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if (entity.getTwinflowId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty twinFlowId");
        if (entity.getSrcTwinStatusId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty srcTwinStatusId");
        if (entity.getDstTwinStatusId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty dstTwinStatusId");

        switch (entityValidateMode) {
            case beforeSave:
                if (entity.getSrcTwinStatus() == null)
                    entity.setSrcTwinStatus(twinStatusService.findEntitySafe(entity.getSrcTwinStatusId()));
                if (entity.getDstTwinStatus() == null)
                    entity.setDstTwinStatus(twinStatusService.findEntitySafe(entity.getDstTwinStatusId()));
            default:
                if (!entity.getSrcTwinStatus().getTwinClassId().equals(entity.getDstTwinStatus().getTwinClassId()))
                    return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " incorrect src/dst status[" + entity.getSrcTwinStatusId() + " > " + entity.getDstTwinStatusId() + "]");
        }
        return true;
    }

    public List<TwinflowTransitionEntity> findValidTransitions(TwinEntity twinEntity) throws ServiceException {
        TwinflowEntity twinflowEntity = twinflowService.loadTwinflow(twinEntity.getTwinClass());
        List<TwinflowTransitionEntity> twinflowTransitionEntityList = twinflowTransitionRepository.findByTwinflowIdAndSrcTwinStatusId(twinflowEntity.getId(), twinEntity.getTwinStatusId());
        ListIterator<TwinflowTransitionEntity> iterator = twinflowTransitionEntityList.listIterator();
        TwinflowTransitionEntity twinflowTransitionEntity;
        while (iterator.hasNext()) {
            twinflowTransitionEntity = iterator.next();
            if (!validateTransition(twinflowTransitionEntity, twinEntity))
                iterator.remove();
        }
        return twinflowTransitionEntityList;
    }

    public boolean validateTransition(TwinflowTransitionEntity twinflowTransitionEntity, TwinEntity twinEntity) throws ServiceException {
        List<TwinflowTransitionValidatorEntity> transitionValidatorEntityList = twinflowTransitionValidatorRepository.findByTwinflowTransitionIdOrderByOrder(twinflowTransitionEntity.getId());
        for (TwinflowTransitionValidatorEntity transitionValidatorEntity : transitionValidatorEntityList) {
            TransitionValidator transitionValidator = featurerService.getFeaturer(transitionValidatorEntity.getTransitionValidatorFeaturer(), TransitionValidator.class);
            if (!transitionValidator.isValid(transitionValidatorEntity.getTransitionValidatorParams(), twinEntity)) {
                log.info(twinflowTransitionEntity.easyLog(EasyLoggable.Level.NORMAL) + " is not valid for " + twinEntity.easyLog(EasyLoggable.Level.NORMAL));
                return false;
            }
        }
        return true;
    }

    @Transactional
    public void performTransition(TwinflowTransitionEntity transitionEntity, TwinUpdate twinUpdate) throws ServiceException {
        if (!validateTransition(transitionEntity, twinUpdate.getDbTwinEntity()))
            throw new ServiceException(ErrorCodeTwins.TWINFLOW_TRANSACTION_INCORRECT);
        if (transitionEntity.isAllowEdit()) {
            twinUpdate.getUpdatedEntity()
                    .setTwinStatusId(transitionEntity.getDstTwinStatusId())
                    .setTwinStatus(transitionEntity.getDstTwinStatus());
            twinService.updateTwin(twinUpdate.getUpdatedEntity(), twinUpdate.getDbTwinEntity(), twinUpdate.getUpdatedFields());
        } else
            twinService.changeStatus(twinUpdate.getDbTwinEntity(), transitionEntity.getDstTwinStatus());
        if (transitionEntity.isAllowAttachment())
            twinService.cudAttachments(twinUpdate.getDbTwinEntity(), twinUpdate.getAttachmentCUD());
        if (transitionEntity.isAllowLinks())
            twinService.cudTwinLinks(twinUpdate.getDbTwinEntity(), twinUpdate.getTwinLinkCUD());

        List<TwinflowTransitionTriggerEntity> transitionTriggerEntityList = twinflowTransitionTriggerRepository.findByTwinflowTransitionIdOrderByOrder(transitionEntity.getId());
        for (TwinflowTransitionTriggerEntity triggerEntity : transitionTriggerEntityList) {
            log.info(triggerEntity.easyLog(EasyLoggable.Level.DETAILED) + " will be triggered");
            TransitionTrigger transitionTrigger = featurerService.getFeaturer(triggerEntity.getTransitionTriggerFeaturer(), TransitionTrigger.class);
            transitionTrigger.run(triggerEntity.getTransitionTriggerParams(), twinUpdate.getUpdatedEntity(), transitionEntity.getSrcTwinStatus(), transitionEntity.getDstTwinStatus());
        }
    }

    @Transactional
    public void performTransition(TwinflowTransitionEntity transitionEntity, TwinEntity twinEntity) throws ServiceException {
        if (!validateTransition(transitionEntity, twinEntity))
            throw new ServiceException(ErrorCodeTwins.TWINFLOW_TRANSACTION_INCORRECT);
        twinService.changeStatus(twinEntity, transitionEntity.getDstTwinStatus());

        List<TwinflowTransitionTriggerEntity> transitionTriggerEntityList = twinflowTransitionTriggerRepository.findByTwinflowTransitionIdOrderByOrder(transitionEntity.getId());
        for (TwinflowTransitionTriggerEntity triggerEntity : transitionTriggerEntityList) {
            log.info(triggerEntity.easyLog(EasyLoggable.Level.DETAILED) + " will be triggered");
            TransitionTrigger transitionTrigger = featurerService.getFeaturer(triggerEntity.getTransitionTriggerFeaturer(), TransitionTrigger.class);
            transitionTrigger.run(triggerEntity.getTransitionTriggerParams(), twinEntity, transitionEntity.getSrcTwinStatus(), transitionEntity.getDstTwinStatus());
        }
    }
}

