package org.twins.core.featurer.twin.finder;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.domain.search.TwinSearch;

import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TwinFinderByCreatedByUserIdGivenTest extends BaseUnitTest {

    private final TwinFinderByCreatedByUserIdGiven finder = new TwinFinderByCreatedByUserIdGiven();

    @Nested
    class Concat {

        @Test
        void concat_excludeFalse_addsToCreatedByUserIdExcludeList() throws ServiceException {
            var userId1 = UUID.randomUUID();
            var userId2 = UUID.randomUUID();

            var properties = new Properties();
            properties.setProperty("userIds", userId1 + "," + userId2);
            properties.setProperty("exclude", "false");
            var twinSearch = new TwinSearch();

            finder.concat(twinSearch, properties, null);

            assertNotNull(twinSearch.getCreatedByUserIdExcludeList());
            assertTrue(twinSearch.getCreatedByUserIdExcludeList().contains(userId1));
            assertTrue(twinSearch.getCreatedByUserIdExcludeList().contains(userId2));
        }

        @Test
        void concat_excludeTrue_addsToCreatedByUserIdList() throws ServiceException {
            var userId = UUID.randomUUID();

            var properties = new Properties();
            properties.setProperty("userIds", userId.toString());
            properties.setProperty("exclude", "true");
            var twinSearch = new TwinSearch();

            finder.concat(twinSearch, properties, null);

            assertNotNull(twinSearch.getCreatedByUserIdList());
            assertTrue(twinSearch.getCreatedByUserIdList().contains(userId));
        }

        @Test
        void concat_singleUserId_works() throws ServiceException {
            var userId = UUID.randomUUID();

            var properties = new Properties();
            properties.setProperty("userIds", userId.toString());
            properties.setProperty("exclude", "false");
            var twinSearch = new TwinSearch();

            finder.concat(twinSearch, properties, null);

            assertNotNull(twinSearch.getCreatedByUserIdExcludeList());
            assertEquals(1, twinSearch.getCreatedByUserIdExcludeList().size());
        }
    }
}
