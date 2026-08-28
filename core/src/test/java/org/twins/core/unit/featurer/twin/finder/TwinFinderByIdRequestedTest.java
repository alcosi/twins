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

class TwinFinderByIdRequestedTest extends BaseUnitTest {

    private final TwinFinderByIdRequested finder = new TwinFinderByIdRequested();

    @Nested
    class Concat {

        @Test
        void concat_singleId_addsToTwinIdList() throws ServiceException {
            var twinId = UUID.randomUUID();

            var properties = new Properties();
            properties.setProperty("paramKey", "twinId");
            properties.setProperty("exclude", "false");
            var twinSearch = new TwinSearch();
            var namedParams = Map.of("twinId", twinId.toString());

            finder.concat(twinSearch, properties, namedParams);

            assertNotNull(twinSearch.getTwinIdList());
            assertTrue(twinSearch.getTwinIdList().contains(twinId));
        }

        @Test
        void concat_multipleIds_addsAllToTwinIdList() throws ServiceException {
            var twinId1 = UUID.randomUUID();
            var twinId2 = UUID.randomUUID();

            var properties = new Properties();
            properties.setProperty("paramKey", "twinId");
            properties.setProperty("exclude", "false");
            var twinSearch = new TwinSearch();
            var namedParams = Map.of("twinId", twinId1 + "," + twinId2);

            finder.concat(twinSearch, properties, namedParams);

            assertNotNull(twinSearch.getTwinIdList());
            assertEquals(2, twinSearch.getTwinIdList().size());
            assertTrue(twinSearch.getTwinIdList().contains(twinId1));
            assertTrue(twinSearch.getTwinIdList().contains(twinId2));
        }

        @Test
        void concat_excludeTrue_addsToTwinIdExcludeList() throws ServiceException {
            var twinId = UUID.randomUUID();

            var properties = new Properties();
            properties.setProperty("paramKey", "twinId");
            properties.setProperty("exclude", "true");
            var twinSearch = new TwinSearch();
            var namedParams = Map.of("twinId", twinId.toString());

            finder.concat(twinSearch, properties, namedParams);

            assertNotNull(twinSearch.getTwinIdExcludeList());
            assertTrue(twinSearch.getTwinIdExcludeList().contains(twinId));
            assertNull(twinSearch.getTwinIdList());
        }

        @Test
        void concat_emptyId_doesNotModifySearch() throws ServiceException {
            var properties = new Properties();
            properties.setProperty("paramKey", "twinId");
            properties.setProperty("exclude", "false");
            var twinSearch = new TwinSearch();
            var namedParams = Map.of("twinId", "");

            finder.concat(twinSearch, properties, namedParams);

            assertNull(twinSearch.getTwinIdList());
            assertNull(twinSearch.getTwinIdExcludeList());
        }

        @Test
        void concat_missingIdNotRequired_doesNotModifySearch() throws ServiceException {
            var properties = new Properties();
            properties.setProperty("paramKey", "twinId");
            properties.setProperty("exclude", "false");
            properties.setProperty("required", "false");
            var twinSearch = new TwinSearch();

            finder.concat(twinSearch, properties, Map.of());

            assertNull(twinSearch.getTwinIdList());
            assertNull(twinSearch.getTwinIdExcludeList());
        }
    }
}
