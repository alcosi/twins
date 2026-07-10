package org.twins.core.featurer.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.service.twin.TwinService;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SchedulerConsistencyCheckPermissionSchemaDetectMismatchesTest extends BaseUnitTest {

    @Mock
    private TwinService twinService;

    private SchedulerConsistencyCheckPermissionSchemaDetectMismatches checker;

    @BeforeEach
    void setUp() {
        checker = new SchedulerConsistencyCheckPermissionSchemaDetectMismatches(twinService);
    }

    @Nested
    class ConsistencyCheckName {

        @Test
        void consistencyCheckName_returnsExpectedName() {
            assertEquals("twin.permission_schema_id", checker.consistencyCheckName());
        }
    }

    @Nested
    class InvalidRecordsCount {

        @Test
        void invalidRecordsCount_delegatesToTwinService() {
            when(twinService.countPermissionSchemaMismatches()).thenReturn(15L);

            var result = checker.invalidRecordsCount();

            assertEquals(15L, result);
        }

        @Test
        void invalidRecordsCount_returnsZeroWhenNone() {
            when(twinService.countPermissionSchemaMismatches()).thenReturn(0L);

            var result = checker.invalidRecordsCount();

            assertEquals(0L, result);
        }
    }

    @Nested
    class ProcessTask {

        @Test
        void processTask_noInvalidRecords_returnsZeroMessage() {
            when(twinService.countPermissionSchemaMismatches()).thenReturn(0L);

            var result = checker.processTask(new Properties());

            assertTrue(result.contains("0 invalid"));
            assertTrue(result.contains("twin.permission_schema_id"));
        }

        @Test
        void processTask_withInvalidRecords_returnsCountMessage() {
            when(twinService.countPermissionSchemaMismatches()).thenReturn(3L);

            var result = checker.processTask(new Properties());

            assertTrue(result.contains("3 invalid"));
            assertTrue(result.contains("twin.permission_schema_id"));
        }
    }
}
