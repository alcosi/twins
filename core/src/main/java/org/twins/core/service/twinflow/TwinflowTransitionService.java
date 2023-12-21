package org.twins.core.service.twinflow;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.LoggerUtils;
import org.cambium.featurer.FeaturerService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusTransitionTriggerRepository;
import org.twins.core.dao.twinflow.*;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.TwinCreate;
import org.twins.core.domain.TwinOperation;
import org.twins.core.domain.TwinUpdate;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.transition.TransitionContext;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.transition.trigger.TransitionTrigger;
import org.twins.core.featurer.transition.validator.TransitionValidator;
import org.twins.core.service.EntitySecureFindServiceImpl;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.factory.TwinFactoryService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twin.TwinStatusService;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwinflowTransitionService extends EntitySecureFindServiceImpl<TwinflowTransitionEntity> {
    final TwinflowTransitionRepository twinflowTransitionRepository;
    final TwinflowTransitionValidatorRepository twinflowTransitionValidatorRepository;
    final TwinflowTransitionTriggerRepository twinflowTransitionTriggerRepository;
    final TwinStatusTransitionTriggerRepository twinStatusTransitionTriggerRepository;
    final TwinClassService twinClassService;
    final TwinFactoryService twinFactoryService;
    final TwinStatusService twinStatusService;
    @Lazy
    final TwinService twinService;
    final TwinflowService twinflowService;
    @Lazy
    final FeaturerService featurerService;
    @Lazy
    final AuthService authService;

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

    public Map<UUID, TwinflowTransitionEntity> findTransitionsByAlias(String transitionAlias) throws ServiceException {
        List<TwinflowTransitionEntity> transitionEntityList = twinflowTransitionRepository.findByTwinflowTransitionAliasId(transitionAlias);
        Map<UUID, TwinflowTransitionEntity> ret = new HashMap<>(); //key is srcStatus
        for (TwinflowTransitionEntity transitionEntity : transitionEntityList) {
            if (validateEntity(transitionEntity, EntitySmartService.EntityValidateMode.afterRead))
                ret.put(transitionEntity.getSrcTwinStatusId(), transitionEntity);
        }
//        Iterator<TwinflowTransitionEntity> iter = transitionEntityList.iterator();
//        while (iter.hasNext()) {
//            TwinflowTransitionEntity transitionEntity = iter.next();
//            if (!validateEntity(transitionEntity, EntitySmartService.EntityValidateMode.afterRead))
//                iter.remove();
//        }
        return ret;
    }

    public void validateTransition(TransitionContext transitionContext) throws ServiceException {
        List<TwinflowTransitionValidatorEntity> transitionValidatorEntityList = twinflowTransitionValidatorRepository.findByTwinflowTransitionIdOrderByOrder(transitionContext.getTransitionEntity().getId());
        for (TwinEntity twinEntity : transitionContext.getTargetTwinList().values())
            if (!validateTransition(transitionContext.getTransitionEntity(), transitionValidatorEntityList, twinEntity))
                throw new ServiceException(ErrorCodeTwins.TWINFLOW_TRANSACTION_INCORRECT);
    }

    public boolean validateTransition(TwinflowTransitionEntity twinflowTransitionEntity, TwinEntity twinEntity) throws ServiceException {
        List<TwinflowTransitionValidatorEntity> transitionValidatorEntityList = twinflowTransitionValidatorRepository.findByTwinflowTransitionIdOrderByOrder(twinflowTransitionEntity.getId());
        return validateTransition(twinflowTransitionEntity, transitionValidatorEntityList, twinEntity);
    }

    public boolean validateTransition(TwinflowTransitionEntity twinflowTransitionEntity, List<TwinflowTransitionValidatorEntity> transitionValidatorEntityList, TwinEntity twinEntity) throws ServiceException {
        for (TwinflowTransitionValidatorEntity transitionValidatorEntity : transitionValidatorEntityList) {
            TransitionValidator transitionValidator = featurerService.getFeaturer(transitionValidatorEntity.getTransitionValidatorFeaturer(), TransitionValidator.class);
            if (!transitionValidator.isValid(transitionValidatorEntity.getTransitionValidatorParams(), twinEntity)) {
                log.info(twinflowTransitionEntity.easyLog(EasyLoggable.Level.NORMAL) + " is not valid for " + twinEntity.logShort());
                return false;
            }
        }
        return true;
    }

    @Transactional
    public TransitionResult performTransition(TransitionContext transitionContext) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        validateTransition(transitionContext);
        TransitionResult ret = new TransitionResult();
        if (transitionContext.getTransitionEntity().getInbuiltTwinFactoryId() != null) {
            FactoryContext factoryContext = new FactoryContext()
                    .setInputTwinList(transitionContext.getTargetTwinList().values())
                    .setFields(transitionContext.getFields());
            if (CollectionUtils.isNotEmpty(transitionContext.getNewTwinList())) //new twins must be added to factory content for having possibility to run pipelines for them
                for (TwinCreate twinCreate : transitionContext.getNewTwinList()) {
                    factoryContext.getFactoryItemList().add(new FactoryItem()
                            .setOutputTwin(twinCreate)
                            .setFactoryContext(factoryContext)
                            .setContextTwinList(transitionContext.getTargetTwinList().values().stream().toList()));
                }
            LoggerUtils.traceTreeStart();
            List<TwinOperation> twinFactoryOutput = twinFactoryService.runFactory(transitionContext.getTransitionEntity().getInbuiltTwinFactoryId(), factoryContext);
            LoggerUtils.traceTreeEnd();
            for (TwinOperation twinOperation : twinFactoryOutput) {
                if (twinOperation instanceof TwinCreate twinCreate) {
                    TwinService.TwinCreateResult twinCreateResult = twinService.createTwin(apiUser, twinCreate);
                    ret.addProcessedTwin(twinCreateResult.getCreatedTwin());
                } else if (twinOperation instanceof TwinUpdate twinUpdate) {
                    boolean isProcessedTwin = true;
                    if (transitionContext.getTargetTwinList() != null && transitionContext.getTargetTwinList().containsKey(twinUpdate.getTwinEntity().getId())) {// case when twin was taken from input, we have to force update status from transition
                        twinUpdate.getTwinEntity()
                                .setTwinStatusId(transitionContext.getTransitionEntity().getDstTwinStatusId())
                                .setTwinStatus(transitionContext.getTransitionEntity().getDstTwinStatus());
                        isProcessedTwin = false;
                    }
                    twinService.updateTwin(twinUpdate);
                    if (isProcessedTwin) {
                        ret.addProcessedTwin(twinUpdate.getDbTwinEntity());
                    } else
                        ret.addTransitionedTwin(twinUpdate.getDbTwinEntity());
                }
            }
        } else {
            twinService.changeStatus(transitionContext.getTargetTwinList().values(), transitionContext.getTransitionEntity().getDstTwinStatus());
            ret.setTransitionedTwinList(transitionContext.getTargetTwinList().values().stream().toList());
        }
        runTriggers(transitionContext);
        return ret;
    }

    @Transactional
    public void runTriggers(TransitionContext transitionContext) throws ServiceException {
        TwinflowTransitionEntity transitionEntity = transitionContext.getTransitionEntity();
        List<TwinflowTransitionTriggerEntity> transitionTriggerEntityList = twinflowTransitionTriggerRepository.findByTwinflowTransitionIdOrderByOrder(transitionEntity.getId());
        //todo run status input/output triggers
        for (TwinEntity targetTwin : transitionContext.getTargetTwinList().values())
            for (TwinflowTransitionTriggerEntity triggerEntity : transitionTriggerEntityList) {
                log.info(triggerEntity.easyLog(EasyLoggable.Level.DETAILED) + " will be triggered");
                TransitionTrigger transitionTrigger = featurerService.getFeaturer(triggerEntity.getTransitionTriggerFeaturer(), TransitionTrigger.class);
                transitionTrigger.run(triggerEntity.getTransitionTriggerParams(), targetTwin, transitionEntity.getSrcTwinStatus(), transitionEntity.getDstTwinStatus());
            }
    }

    @Data
    @Accessors(chain = true)
    public static class TransitionResult {
        private List<TwinEntity> transitionedTwinList;
        private List<TwinEntity> processedTwinList;

        public TransitionResult addTransitionedTwin(TwinEntity twinEntity) {
            transitionedTwinList = org.cambium.common.util.CollectionUtils.safeAdd(transitionedTwinList, twinEntity);
            return this;
        }

        public TransitionResult addTransitionedTwin(List<TwinEntity> twinEntityList) {
            if (CollectionUtils.isEmpty(twinEntityList))
                return this;
            if (transitionedTwinList == null)
                transitionedTwinList = new ArrayList<>();
            transitionedTwinList.addAll(twinEntityList);
            return this;
        }

        public TransitionResult addProcessedTwin(TwinEntity twinEntity) {
            processedTwinList = org.cambium.common.util.CollectionUtils.safeAdd(processedTwinList, twinEntity);
            return this;
        }

        public TransitionResult addProcessedTwin(List<TwinEntity> twinEntityList) {
            if (CollectionUtils.isEmpty(twinEntityList))
                return this;
            if (processedTwinList == null)
                processedTwinList = new ArrayList<>();
            processedTwinList.addAll(twinEntityList);
            return this;
        }
    }

//    @Transactional
//    public void performTransition(TwinflowTransitionEntity transitionEntity, List<TwinUpdate> twinUpdateBatch) throws ServiceException {
//        for (TwinUpdate twinUpdate : twinUpdateBatch) {
//            performTransition(transitionEntity, twinUpdate);
//        }
//    }

//    @Transactional
//    public void performTransition(TwinflowTransitionEntity transitionEntity, TwinEntity twinEntity) throws ServiceException {
//        if (!validateTransition(transitionEntity, twinEntity))
//            throw new ServiceException(ErrorCodeTwins.TWINFLOW_TRANSACTION_INCORRECT);
//        twinService.changeStatus(twinEntity, transitionEntity.getDstTwinStatus());
//
//        List<TwinflowTransitionTriggerEntity> transitionTriggerEntityList = twinflowTransitionTriggerRepository.findByTwinflowTransitionIdOrderByOrder(transitionEntity.getId());
//        for (TwinflowTransitionTriggerEntity triggerEntity : transitionTriggerEntityList) {
//            log.info(triggerEntity.easyLog(EasyLoggable.Level.DETAILED) + " will be triggered");
//            TransitionTrigger transitionTrigger = featurerService.getFeaturer(triggerEntity.getTransitionTriggerFeaturer(), TransitionTrigger.class);
//            transitionTrigger.run(triggerEntity.getTransitionTriggerParams(), twinEntity, transitionEntity.getSrcTwinStatus(), transitionEntity.getDstTwinStatus());
//        }
//    }
}

