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
import org.twins.core.dao.domain.DomainBusinessAccountEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twin.TwinStatusTransitionTriggerEntity;
import org.twins.core.dao.twin.TwinStatusTransitionTriggerRepository;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinflow.*;
import org.twins.core.domain.ApiUser;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.transition.trigger.TransitionTrigger;
import org.twins.core.service.EntitySecureFindServiceImpl;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.domain.DomainService;
import org.twins.core.service.twin.TwinStatusService;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwinflowService extends EntitySecureFindServiceImpl<TwinflowEntity> {
    final TwinflowRepository twinflowRepository;
    final TwinflowSchemaRepository twinflowSchemaRepository;
    final TwinflowSchemaMapRepository twinflowSchemaMapRepository;
    final TwinflowTransitionRepository twinflowTransitionRepository;
    final TwinflowTransitionValidatorRepository twinflowTransitionValidatorRepository;
    final TwinStatusTransitionTriggerRepository twinStatusTransitionTriggerRepository;
    @Lazy
    final DomainService domainService;
    final TwinClassService twinClassService;
    final TwinStatusService twinStatusService;
    @Lazy
    final FeaturerService featurerService;
    @Lazy
    final AuthService authService;

    @Override
    public CrudRepository<TwinflowEntity, UUID> entityRepository() {
        return twinflowRepository;
    }

    @Override
    public boolean isEntityReadDenied(TwinflowEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinflowEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if (entity.getTwinClassId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty twinClassId");
        if (entity.getInitialTwinStatusId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty initialTwinStatusId");

        switch (entityValidateMode) {
            case beforeSave:
                if (entity.getTwinClass() == null)
                    entity.setTwinClass(twinClassService.findEntitySafe(entity.getTwinClassId()));
                if (entity.getInitialTwinStatus() == null)
                    entity.setInitialTwinStatus(twinStatusService.findEntitySafe(entity.getInitialTwinStatusId()));
                if (entity.getTwinClassId() != entity.getTwinClass().getId())
                    return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " incorrect dstTwin object");
                if (entity.getInitialTwinStatusId() != entity.getInitialTwinStatus().getId())
                    return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " incorrect srcTwin object");
            default:
                if (!twinClassService.isInstanceOf(entity.getTwinClassId(), entity.getInitialTwinStatus().getTwinClassId()))
                    return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " incorrect initialTwinStatusId[" + entity.getInitialTwinStatusId() + "]");
        }
        return true;
    }

    public UUID checkTwinflowSchemaAllowed(UUID domainId, UUID businessAccountId, UUID twinFlowsSchemaId) throws ServiceException {
        Optional<TwinflowSchemaEntity> permissionSchemaEntity = twinflowSchemaRepository.findById(twinFlowsSchemaId);
        if (permissionSchemaEntity.isEmpty())
            throw new ServiceException(ErrorCodeTwins.UUID_UNKNOWN, "unknown twinFlowsSchemaId[" + twinFlowsSchemaId + "]");
        if (permissionSchemaEntity.get().domainId() != domainId)
            throw new ServiceException(ErrorCodeTwins.TWINFLOW_SCHEMA_NOT_ALLOWED, "twinFlowsSchemaId[" + twinFlowsSchemaId + "] is not allows in domain[" + domainId + "]");
        if (permissionSchemaEntity.get().businessAccountId() != null && permissionSchemaEntity.get().businessAccountId() != businessAccountId)
            throw new ServiceException(ErrorCodeTwins.TWINFLOW_SCHEMA_NOT_ALLOWED, "twinFlowsSchemaId[" + twinFlowsSchemaId + "] is not allows in businessAccount[" + businessAccountId + "]");
        return twinFlowsSchemaId;
    }

    //todo support space
    public TwinflowEntity loadTwinflowsForTwinClass(TwinClassEntity twinClass) throws ServiceException {
        if (twinClass.getTwinflow() != null)
            return twinClass.getTwinflow();
        TwinflowEntity twinflowEntity = getTwinflow(twinClass.getId());
        twinClass.setTwinflow(twinflowEntity);
        return twinflowEntity;
    }

    public void loadTwinflowsForTwinClasses(List<TwinClassEntity> twinClasses) throws ServiceException {
        for(TwinClassEntity twinClass : twinClasses) {
            if (twinClass.getTwinflow() == null) {
                try {
                    TwinflowEntity twinflowEntity = getTwinflow(twinClass.getId());
                    twinClass.setTwinflow(twinflowEntity);
                } catch (ServiceException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    public TwinflowEntity getTwinflow(UUID twinClassId) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        if (apiUser.getBusinessAccount() != null) {
            DomainBusinessAccountEntity domainBusinessAccountEntity = domainService.getDomainBusinessAccountEntitySafe(apiUser.getDomain().getId(), apiUser.getBusinessAccount().getId()); //todo store in apiUser
            return getTwinflow(domainBusinessAccountEntity.getTwinflowSchemaId(), twinClassId);
        }
        return getTwinflow(apiUser.getDomain().getTwinflowSchemaId(), twinClassId);
    }

    public TwinflowEntity getTwinflow(UUID twinflowSchemaId, UUID twinClassId) throws ServiceException {
        TwinflowSchemaMapEntity twinflowSchemaMapEntity = twinflowSchemaMapRepository.findByTwinflowSchemaIdAndTwinClassId(twinflowSchemaId, twinClassId);
        if (twinflowSchemaMapEntity == null) {
            Set<UUID> extendedClasses = twinClassService.findExtendedClasses(twinClassId, false);
            if (CollectionUtils.isEmpty(extendedClasses))
                throw new ServiceException(ErrorCodeTwins.TWINFLOW_SCHEMA_NOT_CONFIGURED, "Twinflow is not configured for twinClass[" + twinClassId + "] in twinflowSchema[" + twinflowSchemaId + "]");
            List<TwinflowSchemaMapEntity> parentClassTwinflowSchemaMapEntityList = twinflowSchemaMapRepository.findByTwinflowSchemaIdAndTwinClassIdIn(twinflowSchemaId, extendedClasses);
            if (CollectionUtils.isEmpty(parentClassTwinflowSchemaMapEntityList))
                throw new ServiceException(ErrorCodeTwins.TWINFLOW_SCHEMA_NOT_CONFIGURED, "Twinflow is not configured for any parent of twinClass[" + twinClassId + "] in twinflowSchema[" + twinflowSchemaId + "]");
            Map<UUID, TwinflowSchemaMapEntity> parentClassTwinflowSchemaMapEntityMap = parentClassTwinflowSchemaMapEntityList.stream().collect(Collectors.toMap(TwinflowSchemaMapEntity::getTwinClassId, Function.identity()));
            for (UUID extendedClassId : extendedClasses) { //set must be sorter
                twinflowSchemaMapEntity = parentClassTwinflowSchemaMapEntityMap.get(extendedClassId);
                if (twinflowSchemaMapEntity != null) {
                    log.info("Twinflow was detected for parent class[" + extendedClassId + "]");
                    return twinflowSchemaMapEntity.getTwinflow();
                }
            }
        }
        if (twinflowSchemaMapEntity == null)
            throw new ServiceException(ErrorCodeTwins.TWINFLOW_SCHEMA_NOT_CONFIGURED, "Twinflow is not configured for twinClass[" + twinClassId + "] in twinflowSchema[" + twinflowSchemaId + "]");
        return twinflowSchemaMapEntity.getTwinflow();
    }

    public void runTwinStatusTransitionTriggers(TwinEntity twinEntity, TwinStatusEntity srcStatusEntity, TwinStatusEntity dstStatusEntity) throws ServiceException {
        List<TwinStatusTransitionTriggerEntity> triggerEntityList;
        UUID srcStatusId = srcStatusEntity != null ? srcStatusEntity.getId() : null;
        UUID dstStatusId = dstStatusEntity != null ? dstStatusEntity.getId() : null;
        if (srcStatusId != null && !srcStatusId.equals(dstStatusId)) { // outgoing triggers
            triggerEntityList = twinStatusTransitionTriggerRepository.findAllByTwinStatusIdAndTypeAndActiveOrderByOrder(srcStatusId, TwinStatusTransitionTriggerEntity.TransitionType.outgoing, true);
            runTriggers(twinEntity, triggerEntityList, srcStatusEntity, dstStatusEntity);
        }
        if (dstStatusId != null && !dstStatusId.equals(srcStatusId)) { // incoming triggers
            triggerEntityList = twinStatusTransitionTriggerRepository.findAllByTwinStatusIdAndTypeAndActiveOrderByOrder(dstStatusId, TwinStatusTransitionTriggerEntity.TransitionType.incoming, true);
            runTriggers(twinEntity, triggerEntityList, srcStatusEntity, dstStatusEntity);
        }
    }

    private void runTriggers(TwinEntity twinEntity, List<TwinStatusTransitionTriggerEntity> twinStatusTransitionTriggerEntityList, TwinStatusEntity srcStatusEntity, TwinStatusEntity dstStatusEntity) throws ServiceException {
        if (CollectionUtils.isEmpty(twinStatusTransitionTriggerEntityList))
            return;
        for (TwinStatusTransitionTriggerEntity triggerEntity : twinStatusTransitionTriggerEntityList) {
            log.info(triggerEntity.easyLog(EasyLoggable.Level.DETAILED) + " will be triggered");
            TransitionTrigger transitionTrigger = featurerService.getFeaturer(triggerEntity.getTransitionTriggerFeaturer(), TransitionTrigger.class);
            transitionTrigger.run(triggerEntity.getTransitionTriggerParams(), twinEntity, srcStatusEntity, dstStatusEntity);
        }
    }
}

