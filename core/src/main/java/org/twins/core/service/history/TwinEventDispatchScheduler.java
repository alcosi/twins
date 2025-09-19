package org.twins.core.service.history;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.util.LoggerUtils;
import org.cambium.featurer.FeaturerService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.history.HistoryRepository;
import org.twins.core.featurer.dispatcher.Dispatcher;
import org.twins.core.service.domain.DomainService;

import java.sql.Timestamp;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TwinEventDispatchScheduler {
    final ApplicationContext applicationContext;
    @Qualifier("twinEventDispatchTaskExecutor")
    final TaskExecutor taskExecutor;

    private final HistoryService historyService;
    private final FeaturerService featurerService;
    private final DomainService domainService;

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
            List<HistoryRepository.TwinUsersProjection> twinsForNotification = historyService.getHistoryItemsForDispatching(before, collectBatchSize);
            //todo paging???
            if (twinsForNotification.isEmpty()) {
                log.debug("No twins for notification");
                return;
            }
            DomainEntity domain = domainService.findEntitySafe(twinsForNotification.getFirst().getDomainId()); //todo can there be multiple domains?
//            Dispatcher dispatcher = featurerService.getFeaturer(domain.getDispatcherFeaturer(), Dispatcher.class);
//            for (HistoryRepository.TwinUsersProjection twin : twinsForNotification) {
//                TwinEventDispatchTask dispatchTask = applicationContext.getBean(TwinEventDispatchTask.class, dispatcher, domain.getDispatcherFeaturerParams(), twin);
//                taskExecutor.execute(dispatchTask);
//
//            }
        } catch (Exception ex) {
            log.error("Error during collecting history items for notification", ex);
        } finally {
            LoggerUtils.cleanMDC();
        }
    }
}
