package org.twins.core.service.notification;

import alcosi.notification_manager.v1.Receiver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dao.notification.HistoryNotificationSchemaMapEntity;
import org.twins.core.dao.notification.NotificationChannelEntity;
import org.twins.core.featurer.notificator.notifier.Notifier;

import java.util.*;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class NotificationService {

    private final HistoryNotificationSchemaMapService notificationSchemaMapService;
    private final FeaturerService featurerService;

    //todo on scheduler
//    @Scheduled(fixedRate = 5000)
    @Transactional
    public void collect() throws ServiceException {
        List<HistoryEntity> historyList = new ArrayList<>(); //todo get histories in statuses NOT SEND
        List<Receiver.SendNotificationCommand> notificationCommandList = new ArrayList<>();

        UUID notificationSchemaId = UUID.fromString("167261cd-ca6f-45dc-a7d5-cf4d2581b652");
        Set<HistoryNotificationSchemaMapEntity> schemaMaps = notificationSchemaMapService.getByNotificationSchemaAndEventCodes(notificationSchemaId, List.of("PROJECT_ADD", "TASK_ADD_COMMENT"));

        //todo нужно как-то определять, какою schemeNotificationMap вызввать для history
        HistoryNotificationSchemaMapEntity historyNotificationSchemaMapEntity = schemaMaps.stream().findFirst().get();

        //grouping history by schema map
        Map<HistoryNotificationSchemaMapEntity, List<HistoryEntity>> map = new HashMap<>();

        for (HistoryNotificationSchemaMapEntity schemaMap : map.keySet()) {
            List<HistoryEntity> historyEntityList = map.get(schemaMap);

            NotificationChannelEntity notificationChannel = historyNotificationSchemaMapEntity.getNotificationChannelEvent().getNotificationChannel();
            Notifier notifier = featurerService.getFeaturer(notificationChannel.getNotifierFeaturer(), Notifier.class);
            notifier.notify(historyEntityList, schemaMap, notificationChannel.getNotifierParams());
        }

        for (HistoryEntity history : historyList) {
//            history.setStatus("SEND");
        }
//            historyService.saveSafe(history);
    }
}
