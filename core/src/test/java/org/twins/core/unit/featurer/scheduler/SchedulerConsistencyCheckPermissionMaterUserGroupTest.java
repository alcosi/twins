package org.twins.core.featurer.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.permission.PermissionMaterUserGroupRepository;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SchedulerConsistencyCheckPermissionMaterUserGroupTest extends BaseUnitTest {

    @Mock
    private PermissionMaterUserGroupRepository permissionMaterUserGroupRepository;

    private SchedulerConsistencyCheckPermissionMaterUserGroup checker;

    @BeforeEach
    void setUp() {
        checker = new SchedulerConsistencyCheckPermissionMaterUserGroup(permissionMaterUserGroupRepository);
    }

    @Nested
    class ConsistencyCheckName {

        @Test
        void consistencyCheckName_returnsExpectedName() {
            assertEquals("permission_mater_user_group.grants_count", checker.consistencyCheckName());
        }
    }

    @Nested
    class InvalidRecordsCount {

        @Test
        void invalidRecordsCount_delegatesToRepository() {
            when(permissionMaterUserGroupRepository.countInvalidGrantsCount()).thenReturn(6L);

            var result = checker.invalidRecordsCount();

            assertEquals(6L, result);
        }

        @Test
        void invalidRecordsCount_returnsZeroWhenNone() {
            when(permissionMaterUserGroupRepository.countInvalidGrantsCount()).thenReturn(0L);

            var result = checker.invalidRecordsCount();

            assertEquals(0L, result);
        }
    }

    @Nested
    class ProcessTask {

        @Test
        void processTask_noInvalidRecords_returnsZeroMessage() {
            when(permissionMaterUserGroupRepository.countInvalidGrantsCount()).thenReturn(0L);

            var result = checker.processTask(new Properties());

            assertTrue(result.contains("0 invalid"));
            assertTrue(result.contains("permission_mater_user_group.grants_count"));
        }

        @Test
        void processTask_withInvalidRecords_returnsCountMessage() {
            when(permissionMaterUserGroupRepository.countInvalidGrantsCount()).thenReturn(8L);

            var result = checker.processTask(new Properties());

            assertTrue(result.contains("8 invalid"));
            assertTrue(result.contains("permission_mater_user_group.grants_count"));
        }
    }
}
