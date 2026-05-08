package org.twins.core.featurer.twin.finder;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.domain.search.TwinSearch;

import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TwinFinderByAssigneeUserIdGivenTest extends BaseUnitTest {

    private final TwinFinderByAssigneeUserIdGiven finder = new TwinFinderByAssigneeUserIdGiven();

    @Nested
    class Concat {

        @Test
        void concat_excludeFalse_addsToAssigneeUserIdExcludeList() throws ServiceException {
            var userId1 = UUID.randomUUID();
            var userId2 = UUID.randomUUID();

            var properties = new Properties();
            properties.setProperty("userIds", userId1 + "," + userId2);
            properties.setProperty("exclude", "false");
            var twinSearch = new TwinSearch();

            finder.concat(twinSearch, properties, null);

            assertNotNull(twinSearch.getAssigneeUserIdExcludeList());
            assertTrue(twinSearch.getAssigneeUserIdExcludeList().contains(userId1));
            assertTrue(twinSearch.getAssigneeUserIdExcludeList().contains(userId2));
        }

        @Test
        void concat_excludeTrue_addsToAssigneeUserIdList() throws ServiceException {
            var userId = UUID.randomUUID();

            var properties = new Properties();
            properties.setProperty("userIds", userId.toString());
            properties.setProperty("exclude", "true");
            var twinSearch = new TwinSearch();

            finder.concat(twinSearch, properties, null);

            assertNotNull(twinSearch.getAssigneeUserIdList());
            assertTrue(twinSearch.getAssigneeUserIdList().contains(userId));
        }

        @Test
        void concat_singleUserId_works() throws ServiceException {
            var userId = UUID.randomUUID();

            var properties = new Properties();
            properties.setProperty("userIds", userId.toString());
            properties.setProperty("exclude", "false");
            var twinSearch = new TwinSearch();

            finder.concat(twinSearch, properties, null);

            assertNotNull(twinSearch.getAssigneeUserIdExcludeList());
            assertEquals(1, twinSearch.getAssigneeUserIdExcludeList().size());
        }
    }
}
