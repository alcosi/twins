package org.twins.core.featurer.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.notification.HistoryNotificationTaskRepository;
import org.twins.core.enums.HistoryNotificationTaskStatus;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SchedulerHistoryNotificationTaskCleanerTest extends BaseUnitTest {

    @Mock
    private HistoryNotificationTaskRepository historyNotificationTaskRepository;

    private SchedulerHistoryNotificationTaskCleaner cleaner;

    @BeforeEach
    void setUp() {
        cleaner = new SchedulerHistoryNotificationTaskCleaner(historyNotificationTaskRepository);
    }

    @Nested
    class CountAll {

        @Test
        void countAll_delegatesToRepository() {
            when(historyNotificationTaskRepository.countAllByStatusIdIn(List.of(HistoryNotificationTaskStatus.SENT)))
                    .thenReturn(9L);

            var result = cleaner.countAll();

            assertEquals(9L, result);
        }
    }

    @Nested
    class DeleteAll {

        @Test
        void deleteAll_delegatesToRepository() {
            cleaner.deleteAll();

            verify(historyNotificationTaskRepository).deleteAllByStatusIdIn(List.of(HistoryNotificationTaskStatus.SENT));
        }
    }

    @Nested
    class DeleteAllByCreatedAtBefore {

        @Test
        void deleteAllByCreatedAtBefore_delegatesToRepository() {
            var timestamp = Timestamp.valueOf(LocalDateTime.now().minusDays(2));

            cleaner.deleteAllByCreatedAtBefore(timestamp);

            verify(historyNotificationTaskRepository).deleteAllByStatusIdInAndCreatedAtBefore(
                    List.of(HistoryNotificationTaskStatus.SENT), timestamp);
        }
    }

    @Nested
    class CountAllByCreatedAtBefore {

        @Test
        void countAllByCreatedAtBefore_delegatesToRepository() {
            var timestamp = Timestamp.valueOf(LocalDateTime.now().minusDays(2));
            when(historyNotificationTaskRepository.countAllByStatusIdInAndCreatedAtBefore(
                    List.of(HistoryNotificationTaskStatus.SENT), timestamp))
                    .thenReturn(4L);

            var result = cleaner.countAllByCreatedAtBefore(timestamp);

            assertEquals(4L, result);
        }
    }

    @Nested
    class ProcessTask {

        @Test
        void processTask_noRecords_returnsZeroMessage() {
            when(historyNotificationTaskRepository.countAllByStatusIdIn(List.of(HistoryNotificationTaskStatus.SENT)))
                    .thenReturn(0L);

            var result = cleaner.processTask(new Properties());

            assertTrue(result.contains("0 task(s)"));
            verify(historyNotificationTaskRepository, never()).deleteAllByStatusIdIn(anyList());
        }

        @Test
        void processTask_withRecordsAndZeroInterval_deletesAll() {
            when(historyNotificationTaskRepository.countAllByStatusIdIn(List.of(HistoryNotificationTaskStatus.SENT)))
                    .thenReturn(6L);

            var result = cleaner.processTask(new Properties());

            assertTrue(result.contains("6 task(s)"));
            verify(historyNotificationTaskRepository).deleteAllByStatusIdIn(List.of(HistoryNotificationTaskStatus.SENT));
        }

        @Test
        void processTask_withInterval_deletesOlderRecords() {
            var properties = new Properties();
            properties.put("interval", "DAYS:5");
            var timestampCaptor = ArgumentCaptor.forClass(Timestamp.class);
            when(historyNotificationTaskRepository.countAllByStatusIdIn(List.of(HistoryNotificationTaskStatus.SENT)))
                    .thenReturn(10L);
            when(historyNotificationTaskRepository.countAllByStatusIdInAndCreatedAtBefore(
                    eq(List.of(HistoryNotificationTaskStatus.SENT)), timestampCaptor.capture()))
                    .thenReturn(3L);

            var result = cleaner.processTask(properties);

            assertTrue(result.contains("3 task(s)"));
            verify(historyNotificationTaskRepository).countAllByStatusIdInAndCreatedAtBefore(
                    eq(List.of(HistoryNotificationTaskStatus.SENT)), any(Timestamp.class));
            assertTrue(timestampCaptor.getValue().before(new Date()));
            verify(historyNotificationTaskRepository, never()).deleteAllByStatusIdIn(anyList());
        }

        @Test
        void processTask_exception_propagates() {
            when(historyNotificationTaskRepository.countAllByStatusIdIn(List.of(HistoryNotificationTaskStatus.SENT)))
                    .thenThrow(new RuntimeException("test error"));

            assertThrows(RuntimeException.class, () -> cleaner.processTask(new Properties()));
        }
    }
}
