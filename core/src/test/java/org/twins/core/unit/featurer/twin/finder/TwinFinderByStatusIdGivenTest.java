package org.twins.core.featurer.twin.finder;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.domain.search.TwinSearch;

import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TwinFinderByStatusIdGivenTest extends BaseUnitTest {

    private final TwinFinderByStatusIdGiven finder = new TwinFinderByStatusIdGiven();

    @Nested
    class Concat {

        @Test
        void concat_excludeFalse_addsToStatusIdList() throws ServiceException {
            var statusId1 = UUID.randomUUID();
            var statusId2 = UUID.randomUUID();

            var properties = new Properties();
            properties.setProperty("statusIds", statusId1 + "," + statusId2);
            properties.setProperty("exclude", "false");
            var twinSearch = new TwinSearch();

            finder.concat(twinSearch, properties, null);

            assertNotNull(twinSearch.getStatusIdList());
            assertTrue(twinSearch.getStatusIdList().contains(statusId1));
            assertTrue(twinSearch.getStatusIdList().contains(statusId2));
        }

        @Test
        void concat_excludeTrue_addsToStatusIdExcludeList() throws ServiceException {
            var statusId = UUID.randomUUID();

            var properties = new Properties();
            properties.setProperty("statusIds", statusId.toString());
            properties.setProperty("exclude", "true");
            var twinSearch = new TwinSearch();

            finder.concat(twinSearch, properties, null);

            assertNotNull(twinSearch.getStatusIdExcludeList());
            assertTrue(twinSearch.getStatusIdExcludeList().contains(statusId));
            assertNull(twinSearch.getStatusIdList());
        }

        @Test
        void concat_singleStatusId_works() throws ServiceException {
            var statusId = UUID.randomUUID();

            var properties = new Properties();
            properties.setProperty("statusIds", statusId.toString());
            properties.setProperty("exclude", "false");
            var twinSearch = new TwinSearch();

            finder.concat(twinSearch, properties, null);

            assertNotNull(twinSearch.getStatusIdList());
            assertEquals(1, twinSearch.getStatusIdList().size());
        }
    }
}
