package org.twins.core.featurer.twin.finder;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.domain.search.TwinSearch;

import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TwinFinderByStatusIdRequestedTest extends BaseUnitTest {

    private final TwinFinderByStatusIdRequested finder = new TwinFinderByStatusIdRequested();

    @Nested
    class Concat {

        @Test
        void concat_singleStatusId_addsToStatusIdList() throws ServiceException {
            var statusId = UUID.randomUUID();

            var properties = new Properties();
            properties.setProperty("paramKey", "statusId");
            properties.setProperty("exclude", "false");
            var twinSearch = new TwinSearch();
            var namedParams = Map.of("statusId", statusId.toString());

            finder.concat(twinSearch, properties, namedParams);

            assertNotNull(twinSearch.getStatusIdList());
            assertTrue(twinSearch.getStatusIdList().contains(statusId));
        }

        @Test
        void concat_multipleStatusIds_addsAllToStatusIdList() throws ServiceException {
            var statusId1 = UUID.randomUUID();
            var statusId2 = UUID.randomUUID();

            var properties = new Properties();
            properties.setProperty("paramKey", "statusId");
            properties.setProperty("exclude", "false");
            var twinSearch = new TwinSearch();
            var namedParams = Map.of("statusId", statusId1 + "," + statusId2);

            finder.concat(twinSearch, properties, namedParams);

            assertNotNull(twinSearch.getStatusIdList());
            assertEquals(2, twinSearch.getStatusIdList().size());
            assertTrue(twinSearch.getStatusIdList().contains(statusId1));
            assertTrue(twinSearch.getStatusIdList().contains(statusId2));
        }

        @Test
        void concat_excludeTrue_addsToStatusIdExcludeList() throws ServiceException {
            var statusId = UUID.randomUUID();

            var properties = new Properties();
            properties.setProperty("paramKey", "statusId");
            properties.setProperty("exclude", "true");
            var twinSearch = new TwinSearch();
            var namedParams = Map.of("statusId", statusId.toString());

            finder.concat(twinSearch, properties, namedParams);

            assertNotNull(twinSearch.getStatusIdExcludeList());
            assertTrue(twinSearch.getStatusIdExcludeList().contains(statusId));
            assertNull(twinSearch.getStatusIdList());
        }

        @Test
        void concat_emptyStatusId_doesNotModifySearch() throws ServiceException {
            var properties = new Properties();
            properties.setProperty("paramKey", "statusId");
            properties.setProperty("exclude", "false");
            var twinSearch = new TwinSearch();
            var namedParams = Map.of("statusId", "");

            finder.concat(twinSearch, properties, namedParams);

            assertNull(twinSearch.getStatusIdList());
            assertNull(twinSearch.getStatusIdExcludeList());
        }

        @Test
        void concat_missingStatusIdNotRequired_doesNotModifySearch() throws ServiceException {
            var properties = new Properties();
            properties.setProperty("paramKey", "statusId");
            properties.setProperty("exclude", "false");
            properties.setProperty("required", "false");
            var twinSearch = new TwinSearch();

            finder.concat(twinSearch, properties, Map.of());

            assertNull(twinSearch.getStatusIdList());
            assertNull(twinSearch.getStatusIdExcludeList());
        }
    }
}
