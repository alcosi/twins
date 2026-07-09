package org.twins.core.featurer.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.permission.PermissionMaterSpaceUserGroupRepository;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SchedulerConsistencyCheckPermissionMaterSpaceUserGroupTest extends BaseUnitTest {

    @Mock
    private PermissionMaterSpaceUserGroupRepository permissionMaterSpaceUserGroupRepository;

    private SchedulerConsistencyCheckPermissionMaterSpaceUserGroup checker;

    @BeforeEach
    void setUp() {
        checker = new SchedulerConsistencyCheckPermissionMaterSpaceUserGroup(permissionMaterSpaceUserGroupRepository);
    }

    @Nested
    class ConsistencyCheckName {

        @Test
        void consistencyCheckName_returnsExpectedName() {
            assertEquals("permission_mater_space_user_group.grants_count", checker.consistencyCheckName());
        }
    }

    @Nested
    class InvalidRecordsCount {

        @Test
        void invalidRecordsCount_delegatesToRepository() {
            when(permissionMaterSpaceUserGroupRepository.countInvalidGrantsCount()).thenReturn(4L);

            var result = checker.invalidRecordsCount();

            assertEquals(4L, result);
        }

        @Test
        void invalidRecordsCount_returnsZeroWhenNone() {
            when(permissionMaterSpaceUserGroupRepository.countInvalidGrantsCount()).thenReturn(0L);

            var result = checker.invalidRecordsCount();

            assertEquals(0L, result);
        }
    }

    @Nested
    class ProcessTask {

        @Test
        void processTask_noInvalidRecords_returnsZeroMessage() {
            when(permissionMaterSpaceUserGroupRepository.countInvalidGrantsCount()).thenReturn(0L);

            var result = checker.processTask(new Properties());

            assertTrue(result.contains("0 invalid"));
            assertTrue(result.contains("permission_mater_space_user_group.grants_count"));
        }

        @Test
        void processTask_withInvalidRecords_returnsCountMessage() {
            when(permissionMaterSpaceUserGroupRepository.countInvalidGrantsCount()).thenReturn(11L);

            var result = checker.processTask(new Properties());

            assertTrue(result.contains("11 invalid"));
            assertTrue(result.contains("permission_mater_space_user_group.grants_count"));
        }
    }
}
