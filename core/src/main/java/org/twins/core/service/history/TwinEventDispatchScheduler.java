package org.twins.core.service.history;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.util.LoggerUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.twins.core.dao.domain.DomainSubscriptionEventEntity;
import org.twins.core.dao.history.HistoryRepository;
import org.twins.core.service.domain.DomainSubscriptionEventService;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.twins.core.dao.domain.SubscriptionEventType.TWIN_UPDATED;

@Service
@Slf4j
@RequiredArgsConstructor
public class TwinEventDispatchScheduler {
    final ApplicationContext applicationContext;
    @Qualifier("twinEventDispatchTaskExecutor")
    final TaskExecutor taskExecutor;

    private final HistoryService historyService;
    private final DomainSubscriptionEventService domainSubscriptionEventService;

    @Value("${history.subscription.collect.interval.milliseconds:10000}")
    private long collectIntervalMilliseconds;

    @Value("${history.subscription.collect.batch.size:10000}")
    private int collectBatchSize;


    @Scheduled(fixedDelayString = "${history.subscription.collect.scheduler.delay:10000}")
    public void collectHistoryItemsForNotification() {
        try {
            /* TODO -
                1. subscription event types should be configurable per domain
                (How to get domain before querying history items?)
                2. collectIntervalMilliseconds should be configurable per domain
             */
            Timestamp before = new Timestamp(System.currentTimeMillis() - collectIntervalMilliseconds);
            List<HistoryRepository.TwinUsersProjection> twinsForNotification = historyService.getHistoryItemsForDispatching(before, collectBatchSize, TWIN_UPDATED);

            //todo paging???
            if (twinsForNotification.isEmpty()) {
                log.debug("No twins for notification");
                return;
            }

            Map<UUID, DomainSubscriptionEventEntity> dseMap = domainSubscriptionEventService
                    .findAllByDomainIdInAndSubscriptionEventTypeId(
                            twinsForNotification.stream()
                                    .map(HistoryRepository.TwinUsersProjection::getDomainId)
                                    .collect(Collectors.toSet()),
                            TWIN_UPDATED
                    ).stream()
                    .collect(Collectors.toMap(
                            DomainSubscriptionEventEntity::getDomainId,
                            dse -> dse
                    ));

            twinsForNotification = twinsForNotification.stream().filter(i -> dseMap.containsKey(i.getDomainId())).toList();

            for (HistoryRepository.TwinUsersProjection twin : twinsForNotification) {
                DomainSubscriptionEventEntity dse = dseMap.get(twin.getDomainId());

                TwinEventDispatchTask dispatchTask = applicationContext.getBean(TwinEventDispatchTask.class, dse.getDispatcherFeaturer(), dse.getDispatcherFeaturerParams(), twin);
                taskExecutor.execute(dispatchTask);
            }
        } catch (Exception ex) {
            log.error("Error during collecting history items for notification", ex);
        } finally {
            LoggerUtils.cleanMDC();
        }
    }
}
