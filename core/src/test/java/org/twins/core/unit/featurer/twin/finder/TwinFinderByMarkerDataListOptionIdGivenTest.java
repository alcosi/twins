package org.twins.core.featurer.twin.finder;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.domain.search.TwinSearch;

import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TwinFinderByMarkerDataListOptionIdGivenTest extends BaseUnitTest {

    private final TwinFinderByMarkerDataListOptionIdGiven finder = new TwinFinderByMarkerDataListOptionIdGiven();

    @Nested
    class Concat {

        @Test
        void concat_excludeFalse_addsToMarkerDataListOptionIdList() throws ServiceException {
            var optionId1 = UUID.randomUUID();
            var optionId2 = UUID.randomUUID();

            var properties = new Properties();
            properties.setProperty("markerDataListOptionIds", optionId1 + "," + optionId2);
            properties.setProperty("exclude", "false");
            var twinSearch = new TwinSearch();

            finder.concat(twinSearch, properties, null);

            assertNotNull(twinSearch.getMarkerDataListOptionIdList());
            assertTrue(twinSearch.getMarkerDataListOptionIdList().contains(optionId1));
            assertTrue(twinSearch.getMarkerDataListOptionIdList().contains(optionId2));
        }

        @Test
        void concat_excludeTrue_addsToMarkerDataListOptionIdExcludeList() throws ServiceException {
            var optionId = UUID.randomUUID();

            var properties = new Properties();
            properties.setProperty("markerDataListOptionIds", optionId.toString());
            properties.setProperty("exclude", "true");
            var twinSearch = new TwinSearch();

            finder.concat(twinSearch, properties, null);

            assertNotNull(twinSearch.getMarkerDataListOptionIdExcludeList());
            assertTrue(twinSearch.getMarkerDataListOptionIdExcludeList().contains(optionId));
            assertNull(twinSearch.getMarkerDataListOptionIdList());
        }

        @Test
        void concat_singleOptionId_works() throws ServiceException {
            var optionId = UUID.randomUUID();

            var properties = new Properties();
            properties.setProperty("markerDataListOptionIds", optionId.toString());
            properties.setProperty("exclude", "false");
            var twinSearch = new TwinSearch();

            finder.concat(twinSearch, properties, null);

            assertNotNull(twinSearch.getMarkerDataListOptionIdList());
            assertEquals(1, twinSearch.getMarkerDataListOptionIdList().size());
        }
    }
}
