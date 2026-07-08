package org.twins.core.featurer.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.attachment.AttachmentDeleteTaskRepository;
import org.twins.core.enums.attachment.AttachmentDeleteTaskStatus;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SchedulerAttachmentDeleteTaskCleanerTest extends BaseUnitTest {

    @Mock
    private AttachmentDeleteTaskRepository attachmentDeleteTaskRepository;

    private SchedulerAttachmentDeleteTaskCleaner cleaner;

    @BeforeEach
    void setUp() {
        cleaner = new SchedulerAttachmentDeleteTaskCleaner(attachmentDeleteTaskRepository);
    }

    @Nested
    class CountAll {

        @Test
        void countAll_delegatesToRepository() {
            when(attachmentDeleteTaskRepository.countAllByStatusIn(List.of(AttachmentDeleteTaskStatus.DONE)))
                    .thenReturn(5L);

            var result = cleaner.countAll();

            assertEquals(5L, result);
        }

        @Test
        void countAll_returnsZeroWhenNoRecords() {
            when(attachmentDeleteTaskRepository.countAllByStatusIn(List.of(AttachmentDeleteTaskStatus.DONE)))
                    .thenReturn(0L);

            var result = cleaner.countAll();

            assertEquals(0L, result);
        }
    }

    @Nested
    class DeleteAll {

        @Test
        void deleteAll_delegatesToRepository() {
            cleaner.deleteAll();

            verify(attachmentDeleteTaskRepository).deleteAllByStatusIn(List.of(AttachmentDeleteTaskStatus.DONE));
        }
    }

    @Nested
    class DeleteAllByCreatedAtBefore {

        @Test
        void deleteAllByCreatedAtBefore_delegatesToRepository() {
            var timestamp = Timestamp.valueOf(LocalDateTime.now().minusDays(3));

            cleaner.deleteAllByCreatedAtBefore(timestamp);

            verify(attachmentDeleteTaskRepository).deleteAllByStatusInAndCreatedAtBefore(
                    List.of(AttachmentDeleteTaskStatus.DONE), timestamp);
        }
    }

    @Nested
    class CountAllByCreatedAtBefore {

        @Test
        void countAllByCreatedAtBefore_delegatesToRepository() {
            var timestamp = Timestamp.valueOf(LocalDateTime.now().minusDays(3));
            when(attachmentDeleteTaskRepository.countAllByStatusInAndCreatedAtBefore(
                    List.of(AttachmentDeleteTaskStatus.DONE), timestamp))
                    .thenReturn(2L);

            var result = cleaner.countAllByCreatedAtBefore(timestamp);

            assertEquals(2L, result);
        }
    }

    @Nested
    class ProcessTask {

        @Test
        void processTask_noRecords_returnsZeroMessage() {
            var properties = new Properties();
            when(attachmentDeleteTaskRepository.countAllByStatusIn(List.of(AttachmentDeleteTaskStatus.DONE)))
                    .thenReturn(0L);

            var result = cleaner.processTask(properties);

            assertTrue(result.contains("0 task(s)"));
            verify(attachmentDeleteTaskRepository, never()).deleteAllByStatusIn(anyList());
        }

        @Test
        void processTask_withRecordsAndZeroInterval_deletesAll() {
            var properties = new Properties();
            when(attachmentDeleteTaskRepository.countAllByStatusIn(List.of(AttachmentDeleteTaskStatus.DONE)))
                    .thenReturn(3L);

            var result = cleaner.processTask(properties);

            assertTrue(result.contains("3 task(s)"));
            verify(attachmentDeleteTaskRepository).deleteAllByStatusIn(List.of(AttachmentDeleteTaskStatus.DONE));
        }

        @Test
        void processTask_withRecordsAndInterval_deletesOlderRecords() {
            var properties = new Properties();
            properties.put("interval", "DAYS:3");
            var timestampCaptor = ArgumentCaptor.forClass(Timestamp.class);
            when(attachmentDeleteTaskRepository.countAllByStatusIn(List.of(AttachmentDeleteTaskStatus.DONE)))
                    .thenReturn(10L);
            when(attachmentDeleteTaskRepository.countAllByStatusInAndCreatedAtBefore(
                    eq(List.of(AttachmentDeleteTaskStatus.DONE)), timestampCaptor.capture()))
                    .thenReturn(4L);

            var result = cleaner.processTask(properties);

            assertTrue(result.contains("4 task(s)"));
            verify(attachmentDeleteTaskRepository).countAllByStatusInAndCreatedAtBefore(
                    eq(List.of(AttachmentDeleteTaskStatus.DONE)), any(Timestamp.class));
            assertTrue(timestampCaptor.getValue().before(new Date()));
            verify(attachmentDeleteTaskRepository, never()).deleteAllByStatusIn(anyList());
        }

        @Test
        void processTask_exception_returnsErrorMessage() {
            when(attachmentDeleteTaskRepository.countAllByStatusIn(List.of(AttachmentDeleteTaskStatus.DONE)))
                    .thenThrow(new RuntimeException("test error"));

            var result = cleaner.processTask(new Properties());

            assertTrue(result.contains("Processing tasks failed with exception"));
            assertTrue(result.contains("test error"));
        }
    }
}
