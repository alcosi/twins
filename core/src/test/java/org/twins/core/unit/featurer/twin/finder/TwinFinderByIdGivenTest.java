package org.twins.core.featurer.twin.finder;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.domain.search.TwinSearch;

import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TwinFinderByIdGivenTest extends BaseUnitTest {

    private final TwinFinderByIdGiven finder = new TwinFinderByIdGiven();

    @Nested
    class Concat {

        @Test
        void concat_excludeFalse_addsToTwinIdList() throws ServiceException {
            var twinId1 = UUID.randomUUID();
            var twinId2 = UUID.randomUUID();

            var properties = new Properties();
            properties.setProperty("twinIds", twinId1 + "," + twinId2);
            properties.setProperty("exclude", "false");
            var twinSearch = new TwinSearch();

            finder.concat(twinSearch, properties, null);

            assertNotNull(twinSearch.getTwinIdList());
            assertTrue(twinSearch.getTwinIdList().contains(twinId1));
            assertTrue(twinSearch.getTwinIdList().contains(twinId2));
        }

        @Test
        void concat_excludeTrue_addsToTwinIdExcludeList() throws ServiceException {
            var twinId = UUID.randomUUID();

            var properties = new Properties();
            properties.setProperty("twinIds", twinId.toString());
            properties.setProperty("exclude", "true");
            var twinSearch = new TwinSearch();

            finder.concat(twinSearch, properties, null);

            assertNotNull(twinSearch.getTwinIdExcludeList());
            assertTrue(twinSearch.getTwinIdExcludeList().contains(twinId));
            assertNull(twinSearch.getTwinIdList());
        }

        @Test
        void concat_singleTwinId_works() throws ServiceException {
            var twinId = UUID.randomUUID();

            var properties = new Properties();
            properties.setProperty("twinIds", twinId.toString());
            properties.setProperty("exclude", "false");
            var twinSearch = new TwinSearch();

            finder.concat(twinSearch, properties, null);

            assertNotNull(twinSearch.getTwinIdList());
            assertEquals(1, twinSearch.getTwinIdList().size());
        }
    }
}
