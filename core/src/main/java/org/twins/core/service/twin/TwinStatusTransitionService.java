package org.twins.core.service.twin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.cambium.featurer.FeaturerService;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.twin.*;
import org.twins.core.domain.ApiUser;
import org.twins.core.featurer.transition.trigger.TransitionTrigger;
import org.twins.core.service.auth.AuthService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Lazy
@Slf4j
@Service
@RequiredArgsConstructor
public class TwinStatusTransitionService {
    private final TwinStatusTransitionTriggerRepository twinStatusTransitionTriggerRepository;
    private final FeaturerService featurerService;
    private final TwinStatusTransitionTriggerTaskRepository twinflowTransitionTriggerTaskRepository;
    @Lazy
    private final AuthService authService;

    private final EntitySmartService entitySmartService;

    @Transactional
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
        if (org.apache.commons.collections4.CollectionUtils.isEmpty(twinStatusTransitionTriggerEntityList))
            return;
        List<TwinStatusTransitionTriggerTaskEntity> twinStatusTransitionTriggerTaskList = new ArrayList<>();
        for (TwinStatusTransitionTriggerEntity triggerEntity : twinStatusTransitionTriggerEntityList) {
            if (triggerEntity.isAsync()) {
                twinStatusTransitionTriggerTaskList.add(new TwinStatusTransitionTriggerTaskEntity()
                        .setTwinStatusTransitionTriggerId(triggerEntity.getId())
                        .setTwinId(twinEntity.getId())
                        .setSrcTwinStatus(srcStatusEntity)
                        .setDstTwinStatus(dstStatusEntity)
                );
            } else {
                log.info(triggerEntity.easyLog(EasyLoggable.Level.DETAILED) + " will be triggered");
                TransitionTrigger transitionTrigger = featurerService.getFeaturer(triggerEntity.getTransitionTriggerFeaturer(), TransitionTrigger.class);
                transitionTrigger.run(triggerEntity.getTransitionTriggerParams(), twinEntity, srcStatusEntity, dstStatusEntity);
            }
        }
        addTasks(twinStatusTransitionTriggerTaskList);
    }

    public void addTasks(Collection<TwinStatusTransitionTriggerTaskEntity> tasks) throws ServiceException {
        if (CollectionUtils.isEmpty(tasks))
            return;
        ApiUser apiUser = authService.getApiUser();
        List<TwinStatusTransitionTriggerTaskEntity> twinStatusTransitionTriggerTaskList = new ArrayList<>();
        for (var task : tasks) {
            task
                    .setRequestId(apiUser.getRequestId()) //we have uniq index on twinId + requestId to avoid conflict runs
                    .setCreatedAt(Timestamp.from(Instant.now()))
                    .setCreatedByUserId(apiUser.getUserId())
                    .setBusinessAccountId(apiUser.getBusinessAccountId());
            if (task.getStatusId() == null)
                task.setStatusId(TwinStatusTransitionTriggerStatus.NEED_START);
            twinStatusTransitionTriggerTaskList.add(task);
        }
        entitySmartService.saveAllAndLog(twinStatusTransitionTriggerTaskList, twinflowTransitionTriggerTaskRepository);
    }
}
