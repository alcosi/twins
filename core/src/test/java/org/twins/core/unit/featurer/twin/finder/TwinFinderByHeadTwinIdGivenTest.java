package org.twins.core.featurer.twin.finder;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.domain.search.TwinSearch;

import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TwinFinderByHeadTwinIdGivenTest extends BaseUnitTest {

    private final TwinFinderByHeadTwinIdGiven finder = new TwinFinderByHeadTwinIdGiven();

    @Nested
    class Concat {

        @Test
        void concat_singleHeadTwinId_addsToSearch() throws ServiceException {
            var headTwinId = UUID.randomUUID();

            var properties = new Properties();
            properties.setProperty("twinIds", headTwinId.toString());
            var twinSearch = new TwinSearch();

            finder.concat(twinSearch, properties, null);

            assertNotNull(twinSearch.getHeadTwinIdList());
            assertTrue(twinSearch.getHeadTwinIdList().contains(headTwinId));
        }

        @Test
        void concat_multipleHeadTwinIds_addsAllToSearch() throws ServiceException {
            var headTwinId1 = UUID.randomUUID();
            var headTwinId2 = UUID.randomUUID();

            var properties = new Properties();
            properties.setProperty("twinIds", headTwinId1 + "," + headTwinId2);
            var twinSearch = new TwinSearch();

            finder.concat(twinSearch, properties, null);

            assertNotNull(twinSearch.getHeadTwinIdList());
            assertEquals(2, twinSearch.getHeadTwinIdList().size());
            assertTrue(twinSearch.getHeadTwinIdList().contains(headTwinId1));
            assertTrue(twinSearch.getHeadTwinIdList().contains(headTwinId2));
        }
    }
}
