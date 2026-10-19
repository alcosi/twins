package org.twins.core.featurer.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.permission.PermissionMaterSpaceUserRepository;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SchedulerConsistencyCheckPermissionMaterSpaceUserTest extends BaseUnitTest {

    @Mock
    private PermissionMaterSpaceUserRepository permissionMaterSpaceUserRepository;

    private SchedulerConsistencyCheckPermissionMaterSpaceUser checker;

    @BeforeEach
    void setUp() {
        checker = new SchedulerConsistencyCheckPermissionMaterSpaceUser(permissionMaterSpaceUserRepository);
    }

    @Nested
    class ConsistencyCheckName {

        @Test
        void consistencyCheckName_returnsExpectedName() {
            assertEquals("permission_mater_space_user.grants_count", checker.consistencyCheckName());
        }
    }

    @Nested
    class InvalidRecordsCount {

        @Test
        void invalidRecordsCount_delegatesToRepository() {
            when(permissionMaterSpaceUserRepository.countInvalidGrantsCount()).thenReturn(5L);

            var result = checker.invalidRecordsCount();

            assertEquals(5L, result);
        }

        @Test
        void invalidRecordsCount_returnsZeroWhenNone() {
            when(permissionMaterSpaceUserRepository.countInvalidGrantsCount()).thenReturn(0L);

            var result = checker.invalidRecordsCount();

            assertEquals(0L, result);
        }
    }

    @Nested
    class ProcessTask {

        @Test
        void processTask_noInvalidRecords_returnsZeroMessage() {
            when(permissionMaterSpaceUserRepository.countInvalidGrantsCount()).thenReturn(0L);

            var result = checker.processTask(new Properties());

            assertTrue(result.contains("0 invalid"));
            assertTrue(result.contains("permission_mater_space_user.grants_count"));
        }

        @Test
        void processTask_withInvalidRecords_returnsCountMessage() {
            when(permissionMaterSpaceUserRepository.countInvalidGrantsCount()).thenReturn(2L);

            var result = checker.processTask(new Properties());

            assertTrue(result.contains("2 invalid"));
            assertTrue(result.contains("permission_mater_space_user.grants_count"));
        }
    }
}
