package org.twins.core.service.history;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.cambium.featurer.FeaturerService;
import org.cambium.featurer.dao.FeaturerEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.twins.core.dao.history.HistoryDispatchStatus;
import org.twins.core.dao.history.HistoryRepository;
import org.twins.core.featurer.dispatcher.Dispatcher;
import org.twins.core.featurer.transition.trigger.TransitionTrigger;
import org.twins.core.featurer.transition.trigger.messaging.rabbitmq.payloads.RabbitMqMessagePayloadTwinUpdateNotification;

import java.util.HashMap;
import java.util.List;

@Component
@Scope("prototype")
@Slf4j
public class TwinEventDispatchTask implements Runnable {
    private final FeaturerEntity dispatcherEntity;
    private final HashMap<String, String> dispatcherParams;
    private final TwinUsersForDispatch twinHistoryProjection;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private FeaturerService featurerService;

    public TwinEventDispatchTask(FeaturerEntity dispatcherEntity, HashMap<String, String> dispatcherParams, TwinUsersForDispatch twinHistoryProjection) {
        this.dispatcherEntity = dispatcherEntity;
        this.dispatcherParams = dispatcherParams;
        this.twinHistoryProjection = twinHistoryProjection;


    }

    @Transactional
    @Override
    public void run() {
        try {
            log.debug("Twin id: {}, users to notify: {}", twinHistoryProjection.getTwinId(), twinHistoryProjection.getUserIds());
            Dispatcher dispatcher = featurerService.getFeaturer(dispatcherEntity, Dispatcher.class);

            RabbitMqMessagePayloadTwinUpdateNotification payload =
                    new RabbitMqMessagePayloadTwinUpdateNotification(twinHistoryProjection.getTwinId(), List.of(twinHistoryProjection.getUserIds()));
            dispatcher.sendMessage(dispatcherParams, payload);
            historyService.updateAllNotified(List.of(twinHistoryProjection.getHistoryIds()), HistoryDispatchStatus.DONE);
        } catch (Exception e) {
            log.error("Error notifying users of twin {}: {}", twinHistoryProjection.getTwinId(), e.getMessage());
            historyService.updateAllNotified(List.of(twinHistoryProjection.getHistoryIds()), HistoryDispatchStatus.FAILED);
        }
    }
}
