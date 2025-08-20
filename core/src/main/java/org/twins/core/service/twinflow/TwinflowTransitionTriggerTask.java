package org.twins.core.service.twinflow;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.LoggerUtils;
import org.cambium.featurer.FeaturerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinflow.TwinflowTransitionTriggerTaskEntity;
import org.twins.core.dao.twinflow.TwinflowTransitionTriggerTaskRepository;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.transition.trigger.TransitionTrigger;
import org.twins.core.service.auth.AuthService;

@Component
@Scope("prototype")
@Slf4j
public class TwinflowTransitionTriggerTask implements Runnable {
    private final TwinflowTransitionTriggerTaskEntity twinflowTransitionTriggerTaskEntity;

    @Autowired
    private TwinflowTransitionTriggerTaskRepository twinflowTransitionTriggerTaskRepository;
    @Autowired
    private AuthService authService;

    @Autowired
    private FeaturerService featurerService;


    public TwinflowTransitionTriggerTask(TwinflowTransitionTriggerTaskEntity twinflowTransitionTriggerTaskEntity) {
        this.twinflowTransitionTriggerTaskEntity = twinflowTransitionTriggerTaskEntity;
    }

    @Override
    public void run() {
        try {
            LoggerUtils.logSession(twinflowTransitionTriggerTaskEntity.getRequestId());
            LoggerUtils.logController("twinflowTransitionTriggerTask$");
            LoggerUtils.logPrefix("TWINFLOW_TRANSITION_TRIGGER_TASK[" + twinflowTransitionTriggerTaskEntity.getId() + "]:");
            log.info("Performing async twinflow transition trigger run: {}", twinflowTransitionTriggerTaskEntity.logDetailed());
            if (twinflowTransitionTriggerTaskEntity.getTwin().getTwinClass().getDomainId() == null) {
                throw new ServiceException(ErrorCodeTwins.FACTORY_INCORRECT, "can not detect domain from input " + twinflowTransitionTriggerTaskEntity.getTwin().logNormal());
            }
            authService.setThreadLocalApiUser(twinflowTransitionTriggerTaskEntity.getTwin().getTwinClass().getDomainId(), twinflowTransitionTriggerTaskEntity.getBusinessAccountId(),
                    twinflowTransitionTriggerTaskEntity.getCreatedByUserId());
            TransitionTrigger transitionTrigger = featurerService.getFeaturer(twinflowTransitionTriggerTaskEntity.getTwinflowTransitionTrigger().getTransitionTriggerFeaturer(),
                    TransitionTrigger.class);
            transitionTrigger.run(twinflowTransitionTriggerTaskEntity.getTwinflowTransitionTrigger().getTransitionTriggerParams(),
                    twinflowTransitionTriggerTaskEntity.getTwin(), twinflowTransitionTriggerTaskEntity.getSrcTwinStatus(),
                    twinflowTransitionTriggerTaskEntity.getTwinflowTransitionTrigger().getTwinflowTransition().getDstTwinStatus());
            //todo - check that all params are filled in correctly
        } catch (Exception e) {
            //correct exception types
        }

    }
}
