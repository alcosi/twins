package org.twins.core.featurer.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.scheduler.SchedulerLogRepository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SchedulerSchedulerLogCleanerTest extends BaseUnitTest {

    @Mock
    private SchedulerLogRepository schedulerLogRepository;

    private SchedulerSchedulerLogCleaner cleaner;

    @BeforeEach
    void setUp() {
        cleaner = new SchedulerSchedulerLogCleaner(schedulerLogRepository);
    }

    @Nested
    class CountAll {

        @Test
        void countAll_delegatesToRepository() {
            when(schedulerLogRepository.count()).thenReturn(7L);

            var result = cleaner.countAll();

            assertEquals(7L, result);
        }
    }

    @Nested
    class DeleteAll {

        @Test
        void deleteAll_delegatesToRepository() {
            cleaner.deleteAll();

            verify(schedulerLogRepository).deleteAll();
        }
    }

    @Nested
    class DeleteAllByCreatedAtBefore {

        @Test
        void deleteAllByCreatedAtBefore_delegatesToRepository() {
            var timestamp = Timestamp.valueOf(LocalDateTime.now().minusDays(5));

            cleaner.deleteAllByCreatedAtBefore(timestamp);

            verify(schedulerLogRepository).deleteAllByCreatedAtBefore(timestamp);
        }
    }

    @Nested
    class CountAllByCreatedAtBefore {

        @Test
        void countAllByCreatedAtBefore_delegatesToRepository() {
            var timestamp = Timestamp.valueOf(LocalDateTime.now().minusDays(5));
            when(schedulerLogRepository.countAllByCreatedAtBefore(timestamp)).thenReturn(3L);

            var result = cleaner.countAllByCreatedAtBefore(timestamp);

            assertEquals(3L, result);
        }
    }

    @Nested
    class ProcessTask {

        @Test
        void processTask_noRecords_returnsZeroMessage() {
            when(schedulerLogRepository.count()).thenReturn(0L);

            var result = cleaner.processTask(new Properties());

            assertTrue(result.contains("0 task(s)"));
            verify(schedulerLogRepository, never()).deleteAll();
        }

        @Test
        void processTask_withRecordsAndZeroInterval_deletesAll() {
            when(schedulerLogRepository.count()).thenReturn(5L);

            var result = cleaner.processTask(new Properties());

            assertTrue(result.contains("5 task(s)"));
            verify(schedulerLogRepository).deleteAll();
        }

        @Test
        void processTask_withInterval_deletesOlderRecords() {
            var properties = new Properties();
            properties.put("interval", "DAYS:7");
            when(schedulerLogRepository.count()).thenReturn(10L);
            var timestampCaptor = ArgumentCaptor.forClass(Timestamp.class);
            when(schedulerLogRepository.countAllByCreatedAtBefore(timestampCaptor.capture())).thenReturn(2L);

            var result = cleaner.processTask(properties);

            assertTrue(result.contains("2 task(s)"));
            verify(schedulerLogRepository).countAllByCreatedAtBefore(any(Timestamp.class));
            assertTrue(timestampCaptor.getValue().before(new Date()));
            verify(schedulerLogRepository, never()).deleteAll();
        }

        @Test
        void processTask_exception_propagates() {
            when(schedulerLogRepository.count()).thenThrow(new RuntimeException("test error"));

            assertThrows(RuntimeException.class, () -> cleaner.processTask(new Properties()));
        }
    }
}
