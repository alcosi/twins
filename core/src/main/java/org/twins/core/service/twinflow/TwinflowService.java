package org.twins.core.service.twinflow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.springframework.context.annotation.Lazy;
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
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.domain.DomainService;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwinflowService {
    final TwinflowRepository twinflowRepository;
    final TwinflowSchemaRepository twinflowSchemaRepository;
    final TwinflowSchemaMapRepository twinflowSchemaMapRepository;
    final TwinStatusTransitionTriggerRepository twinStatusTransitionTriggerRepository;
    @Lazy
    final DomainService domainService;
    final TwinClassService twinClassService;
    @Lazy
    final FeaturerService featurerService;
    @Lazy
    final AuthService authService;

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
    public TwinflowEntity getTwinflowByTwinClass(UUID twinClassId) throws ServiceException {
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
            triggerEntityList = twinStatusTransitionTriggerRepository.findAllByTwinStatusIdAndTypeOrderByOrder(srcStatusId, TwinStatusTransitionTriggerEntity.TransitionType.outgoing);
            runTriggers(twinEntity, triggerEntityList, srcStatusEntity, dstStatusEntity);
        }
        if (dstStatusId != null && !dstStatusId.equals(srcStatusId)) { // incoming triggers
            triggerEntityList = twinStatusTransitionTriggerRepository.findAllByTwinStatusIdAndTypeOrderByOrder(dstStatusId, TwinStatusTransitionTriggerEntity.TransitionType.incoming);
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

