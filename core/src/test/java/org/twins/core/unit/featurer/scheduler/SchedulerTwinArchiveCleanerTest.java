package org.twins.core.featurer.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinArchiveRepository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SchedulerTwinArchiveCleanerTest extends BaseUnitTest {

    @Mock
    private TwinArchiveRepository twinArchiveRepository;

    private SchedulerTwinArchiveCleaner cleaner;

    @BeforeEach
    void setUp() {
        cleaner = new SchedulerTwinArchiveCleaner(twinArchiveRepository);
    }

    @Nested
    class CountAll {

        @Test
        void countAll_delegatesToRepository() {
            when(twinArchiveRepository.count()).thenReturn(12L);

            var result = cleaner.countAll();

            assertEquals(12L, result);
        }
    }

    @Nested
    class DeleteAll {

        @Test
        void deleteAll_delegatesToRepository() {
            cleaner.deleteAll();

            verify(twinArchiveRepository).deleteAll();
        }
    }

    @Nested
    class DeleteAllByCreatedAtBefore {

        @Test
        void deleteAllByCreatedAtBefore_delegatesToRepository() {
            var timestamp = Timestamp.valueOf(LocalDateTime.now().minusDays(10));

            cleaner.deleteAllByCreatedAtBefore(timestamp);

            verify(twinArchiveRepository).deleteAllByCreatedAtBefore(timestamp);
        }
    }

    @Nested
    class CountAllByCreatedAtBefore {

        @Test
        void countAllByCreatedAtBefore_delegatesToRepository() {
            var timestamp = Timestamp.valueOf(LocalDateTime.now().minusDays(10));
            when(twinArchiveRepository.countAllByCreatedAtBefore(timestamp)).thenReturn(6L);

            var result = cleaner.countAllByCreatedAtBefore(timestamp);

            assertEquals(6L, result);
        }
    }

    @Nested
    class ProcessTask {

        @Test
        void processTask_noRecords_returnsZeroMessage() {
            when(twinArchiveRepository.count()).thenReturn(0L);

            var result = cleaner.processTask(new Properties());

            assertTrue(result.contains("0 task(s)"));
            verify(twinArchiveRepository, never()).deleteAll();
        }

        @Test
        void processTask_withRecordsAndZeroInterval_deletesAll() {
            when(twinArchiveRepository.count()).thenReturn(8L);

            var result = cleaner.processTask(new Properties());

            assertTrue(result.contains("8 task(s)"));
            verify(twinArchiveRepository).deleteAll();
        }

        @Test
        void processTask_withInterval_deletesOlderRecords() {
            var properties = new Properties();
            properties.put("interval", "DAYS:30");
            var timestampCaptor = ArgumentCaptor.forClass(Timestamp.class);
            when(twinArchiveRepository.count()).thenReturn(20L);
            when(twinArchiveRepository.countAllByCreatedAtBefore(timestampCaptor.capture())).thenReturn(15L);

            var result = cleaner.processTask(properties);

            assertTrue(result.contains("15 task(s)"));
            verify(twinArchiveRepository).countAllByCreatedAtBefore(any(Timestamp.class));
            assertTrue(timestampCaptor.getValue().before(new Date()));
            verify(twinArchiveRepository, never()).deleteAll();
        }

        @Test
        void processTask_exception_returnsErrorMessage() {
            when(twinArchiveRepository.count()).thenThrow(new RuntimeException("test error"));

            var result = cleaner.processTask(new Properties());

            assertTrue(result.contains("Processing tasks failed with exception"));
            assertTrue(result.contains("test error"));
        }
    }
}
