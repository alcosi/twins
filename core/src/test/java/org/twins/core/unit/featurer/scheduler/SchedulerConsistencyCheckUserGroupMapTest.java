package org.twins.core.featurer.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.usergroup.UserGroupMapRepository;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SchedulerConsistencyCheckUserGroupMapTest extends BaseUnitTest {

    @Mock
    private UserGroupMapRepository userGroupMapRepository;

    private SchedulerConsistencyCheckUserGroupMap checker;

    @BeforeEach
    void setUp() {
        checker = new SchedulerConsistencyCheckUserGroupMap(userGroupMapRepository);
    }

    @Nested
    class ConsistencyCheckName {

        @Test
        void consistencyCheckName_returnsExpectedName() {
            assertEquals("user_group_map.involves_count", checker.consistencyCheckName());
        }
    }

    @Nested
    class InvalidRecordsCount {

        @Test
        void invalidRecordsCount_delegatesToRepository() {
            when(userGroupMapRepository.countInvalidInvolvesCount()).thenReturn(2L);

            var result = checker.invalidRecordsCount();

            assertEquals(2L, result);
        }

        @Test
        void invalidRecordsCount_returnsZeroWhenNone() {
            when(userGroupMapRepository.countInvalidInvolvesCount()).thenReturn(0L);

            var result = checker.invalidRecordsCount();

            assertEquals(0L, result);
        }
    }

    @Nested
    class ProcessTask {

        @Test
        void processTask_noInvalidRecords_returnsZeroMessage() {
            when(userGroupMapRepository.countInvalidInvolvesCount()).thenReturn(0L);

            var result = checker.processTask(new Properties());

            assertTrue(result.contains("0 invalid"));
            assertTrue(result.contains("user_group_map.involves_count"));
        }

        @Test
        void processTask_withInvalidRecords_returnsCountMessage() {
            when(userGroupMapRepository.countInvalidInvolvesCount()).thenReturn(5L);

            var result = checker.processTask(new Properties());

            assertTrue(result.contains("5 invalid"));
            assertTrue(result.contains("user_group_map.involves_count"));
        }
    }
}
