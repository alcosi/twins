package org.twins.core.service.trigger;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.twins.core.dao.trigger.TwinTriggerEntity;
import org.twins.core.dao.trigger.TwinTriggerTaskEntity;
import org.twins.core.dao.trigger.TwinTriggerTaskStatus;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.featurer.transition.trigger.TwinTrigger;
import org.twins.core.service.auth.AuthService;

import java.sql.Timestamp;
import java.time.Instant;

@Component
@Scope("prototype")
@Slf4j
public class TwinTriggerTask implements Runnable {
    private final TwinTriggerTaskEntity twinTriggerTaskEntity;

    @Autowired
    private TwinTriggerTaskService twinTriggerTaskService;

    @Autowired
    private AuthService authService;

    @Autowired
    private FeaturerService featurerService;

    public TwinTriggerTask(TwinTriggerTaskEntity twinTriggerTaskEntity) {
        this.twinTriggerTaskEntity = twinTriggerTaskEntity;
    }

    @Override
    public void run() {
        try {
            log.info("Performing async twin trigger run: {}", twinTriggerTaskEntity.logDetailed());
            TwinEntity twin = twinTriggerTaskEntity.getTwin();
            TwinStatusEntity previousTwinStatus = twinTriggerTaskEntity.getPreviousTwinStatus();
            TwinTriggerEntity twinTrigger = twinTriggerTaskEntity.getTwinTrigger();

            authService.setThreadLocalApiUser(
                    twin.getTwinClass().getDomainId(),
                    twinTriggerTaskEntity.getBusinessAccountId(),
                    twinTriggerTaskEntity.getCreatedByUserId()
            );

            TwinTrigger trigger = featurerService.getFeaturer(twinTrigger.getTwinTriggerFeaturerId(), TwinTrigger.class);
            trigger.run(twinTrigger.getTwinTriggerParam(), twin, previousTwinStatus, null);

            twinTriggerTaskEntity
                    .setStatusId(TwinTriggerTaskStatus.DONE)
                    .setDoneAt(Timestamp.from(Instant.now()));
        } catch (ServiceException e) {
            log.error(e.log());
            twinTriggerTaskEntity
                    .setStatusId(TwinTriggerTaskStatus.FAILED)
                    .setStatusDetails(e.log());
        } catch (Throwable e) {
            log.error("Exception: ", e);
            twinTriggerTaskEntity
                    .setStatusId(TwinTriggerTaskStatus.FAILED)
                    .setStatusDetails(e.getMessage());
        } finally {
            authService.removeThreadLocalApiUser();
            try {
                twinTriggerTaskService.saveSafe(twinTriggerTaskEntity);
            } catch (Exception e) {
                log.error("Failed to save trigger task status: {}", e.getMessage(), e);
            }
        }
    }
}
