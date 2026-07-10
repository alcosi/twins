package org.twins.core.featurer.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.permission.PermissionMaterGlobalRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SchedulerConsistencyCheckPermissionMaterGlobalTest extends BaseUnitTest {

    @Mock
    private PermissionMaterGlobalRepository permissionMaterGlobalRepository;

    private SchedulerConsistencyCheckPermissionMaterGlobal checker;

    @BeforeEach
    void setUp() {
        checker = new SchedulerConsistencyCheckPermissionMaterGlobal(permissionMaterGlobalRepository);
    }

    @Nested
    class ConsistencyCheckName {

        @Test
        void consistencyCheckName_returnsExpectedName() {
            assertEquals("permission_mater_global.grants_count", checker.consistencyCheckName());
        }
    }

    @Nested
    class InvalidRecordsCount {

        @Test
        void invalidRecordsCount_delegatesToRepository() {
            when(permissionMaterGlobalRepository.countInvalidGrantsCount()).thenReturn(3L);

            var result = checker.invalidRecordsCount();

            assertEquals(3L, result);
        }

        @Test
        void invalidRecordsCount_returnsZeroWhenNone() {
            when(permissionMaterGlobalRepository.countInvalidGrantsCount()).thenReturn(0L);

            var result = checker.invalidRecordsCount();

            assertEquals(0L, result);
        }
    }

    @Nested
    class ProcessTask {

        @Test
        void processTask_noInvalidRecords_returnsZeroMessage() {
            when(permissionMaterGlobalRepository.countInvalidGrantsCount()).thenReturn(0L);

            var result = checker.processTask(new java.util.Properties());

            assertTrue(result.contains("0 invalid"));
            assertTrue(result.contains("permission_mater_global.grants_count"));
        }

        @Test
        void processTask_withInvalidRecords_returnsCountMessage() {
            when(permissionMaterGlobalRepository.countInvalidGrantsCount()).thenReturn(7L);

            var result = checker.processTask(new java.util.Properties());

            assertTrue(result.contains("7 invalid"));
            assertTrue(result.contains("permission_mater_global.grants_count"));
        }
    }
}
