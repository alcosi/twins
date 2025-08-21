package org.twins.core.service.twin;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.LoggerUtils;
import org.cambium.featurer.FeaturerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinStatusTransitionTriggerStatus;
import org.twins.core.dao.twin.TwinStatusTransitionTriggerTaskEntity;
import org.twins.core.dao.twin.TwinStatusTransitionTriggerTaskRepository;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.transition.trigger.TransitionTrigger;
import org.twins.core.service.auth.AuthService;

import java.sql.Timestamp;
import java.time.Instant;

@Component
@Scope("prototype")
@Slf4j
public class TwinStatusTransitionTask implements Runnable {
    private final TwinStatusTransitionTriggerTaskEntity twinStatusTransitionTriggerTaskEntity;

    @Autowired
    private TwinStatusTransitionTriggerTaskRepository twinStatusTransitionTriggerTaskRepository;
    @Autowired
    private AuthService authService;

    @Autowired
    private FeaturerService featurerService;


    public TwinStatusTransitionTask(TwinStatusTransitionTriggerTaskEntity twinStatusTransitionTriggerTaskEntity) {
        this.twinStatusTransitionTriggerTaskEntity = twinStatusTransitionTriggerTaskEntity;
    }

    @Override
    public void run() {
        try {
            LoggerUtils.logSession(twinStatusTransitionTriggerTaskEntity.getRequestId());
            LoggerUtils.logController("twinStatusTransitionTriggerTask$");
            LoggerUtils.logPrefix("TWIN_STATUS_TRANSITION_TRIGGER_TASK[" + twinStatusTransitionTriggerTaskEntity.getId() + "]:");
            log.info("Performing async twin status transition trigger run: {}", twinStatusTransitionTriggerTaskEntity.logDetailed());
            if (twinStatusTransitionTriggerTaskEntity.getTwin().getTwinClass().getDomainId() == null) {
                throw new ServiceException(ErrorCodeTwins.FACTORY_INCORRECT, "can not detect domain from input " + twinStatusTransitionTriggerTaskEntity.getTwin().logNormal());
            }
            authService.setThreadLocalApiUser(twinStatusTransitionTriggerTaskEntity.getTwin().getTwinClass().getDomainId(), twinStatusTransitionTriggerTaskEntity.getBusinessAccountId(),
                    twinStatusTransitionTriggerTaskEntity.getCreatedByUserId());
            TransitionTrigger transitionTrigger = featurerService.getFeaturer(twinStatusTransitionTriggerTaskEntity.getTwinStatusTransitionTrigger().getTransitionTriggerFeaturer(), TransitionTrigger.class);
            transitionTrigger.run(twinStatusTransitionTriggerTaskEntity.getTwinStatusTransitionTrigger().getTransitionTriggerParams(), twinStatusTransitionTriggerTaskEntity.getTwin(), twinStatusTransitionTriggerTaskEntity.getSrcTwinStatus(), twinStatusTransitionTriggerTaskEntity.getDstTwinStatus());
            twinStatusTransitionTriggerTaskEntity
                    .setStatusId(TwinStatusTransitionTriggerStatus.DONE)
                    .setDoneAt(Timestamp.from(Instant.now()));
        } catch (ServiceException e) {
            log.error(e.log());
            twinStatusTransitionTriggerTaskEntity
                    .setStatusId(TwinStatusTransitionTriggerStatus.FAILED)
                    .setStatusDetails(e.log());
        } catch (Throwable e) {
            log.error("Exception: ", e);
            twinStatusTransitionTriggerTaskEntity
                    .setStatusId(TwinStatusTransitionTriggerStatus.FAILED)
                    .setStatusDetails(e.getMessage());
        } finally {
            authService.removeThreadLocalApiUser();
            twinStatusTransitionTriggerTaskRepository.save(twinStatusTransitionTriggerTaskEntity);
            LoggerUtils.cleanMDC();
        }

    }
}
