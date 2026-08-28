package org.twins.core.featurer.twin.finder;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.domain.search.TwinSearch;

import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TwinFinderByTagDataListOptionIdGivenTest extends BaseUnitTest {

    private final TwinFinderByTagDataListOptionIdGiven finder = new TwinFinderByTagDataListOptionIdGiven();

    @Nested
    class Concat {

        @Test
        void concat_excludeFalse_addsToTagDataListOptionIdList() throws ServiceException {
            var tagId1 = UUID.randomUUID();
            var tagId2 = UUID.randomUUID();

            var properties = new Properties();
            properties.setProperty("tagDataListOptionIds", tagId1 + "," + tagId2);
            properties.setProperty("exclude", "false");
            var twinSearch = new TwinSearch();

            finder.concat(twinSearch, properties, null);

            assertNotNull(twinSearch.getTagDataListOptionIdList());
            assertTrue(twinSearch.getTagDataListOptionIdList().contains(tagId1));
            assertTrue(twinSearch.getTagDataListOptionIdList().contains(tagId2));
        }

        @Test
        void concat_excludeTrue_addsToTagDataListOptionIdExcludeList() throws ServiceException {
            var tagId = UUID.randomUUID();

            var properties = new Properties();
            properties.setProperty("tagDataListOptionIds", tagId.toString());
            properties.setProperty("exclude", "true");
            var twinSearch = new TwinSearch();

            finder.concat(twinSearch, properties, null);

            assertNotNull(twinSearch.getTagDataListOptionIdExcludeList());
            assertTrue(twinSearch.getTagDataListOptionIdExcludeList().contains(tagId));
            assertNull(twinSearch.getTagDataListOptionIdList());
        }

        @Test
        void concat_singleTagId_works() throws ServiceException {
            var tagId = UUID.randomUUID();

            var properties = new Properties();
            properties.setProperty("tagDataListOptionIds", tagId.toString());
            properties.setProperty("exclude", "false");
            var twinSearch = new TwinSearch();

            finder.concat(twinSearch, properties, null);

            assertNotNull(twinSearch.getTagDataListOptionIdList());
            assertEquals(1, twinSearch.getTagDataListOptionIdList().size());
        }
    }
}
