package org.twins.core.service.history;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.twins.core.dao.history.HistoryRepository;
import org.twins.core.featurer.dispatcher.Dispatcher;
import org.twins.core.featurer.transition.trigger.messaging.rabbitmq.payloads.RabbitMqMessagePayloadTwinUpdateNotification;

import java.util.HashMap;
import java.util.List;

@Component
@Scope("prototype")
@Slf4j
public class TwinEventDispatchTask implements Runnable {
    private final Dispatcher dispatcher;
    private final HashMap<String, String> dispatcherParams;
    private final HistoryRepository.TwinUsersProjection twinHistoryProjection;

    @Autowired
    private HistoryService historyService;

    public TwinEventDispatchTask(Dispatcher dispatcher, HashMap<String, String> dispatcherParams, HistoryRepository.TwinUsersProjection twinHistoryProjection) {
        this.dispatcher = dispatcher;
        this.dispatcherParams = dispatcherParams;
        this.twinHistoryProjection = twinHistoryProjection;


    }

    @Transactional
    @Override
    public void run() {
        try {
            log.debug("Twin id: {}, users to notify: {}", twinHistoryProjection.getTwinId(), twinHistoryProjection.getUserIds());
            RabbitMqMessagePayloadTwinUpdateNotification payload =
                    new RabbitMqMessagePayloadTwinUpdateNotification(twinHistoryProjection.getTwinId(), List.of(twinHistoryProjection.getUserIds()));
            dispatcher.sendMessage(dispatcherParams, payload);
            historyService.updateAllNotified(List.of(twinHistoryProjection.getHistoryIds()), true);
        } catch (Exception e) {
            log.error("Error notifying users of twin {}: {}", twinHistoryProjection.getTwinId(), e.getMessage());
            historyService.updateAllNotified(List.of(twinHistoryProjection.getHistoryIds()), false);
        }
    }
}
