package org.twins.core.featurer.twin.finder;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.domain.search.TwinSearch;

import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TwinFinderByLinkGivenTest extends BaseUnitTest {

    private final TwinFinderByLinkGiven finder = new TwinFinderByLinkGiven();

    @Nested
    class Concat {

        @Test
        void concat_notExcludeAllOf_addsToDstLinksAllOfListWithNullDstTwinIds() throws ServiceException {
            var linkId = UUID.randomUUID();

            var properties = new Properties();
            properties.setProperty("linkId", linkId.toString());
            properties.setProperty("exclude", "false");
            properties.setProperty("anyOfList", "false");
            var twinSearch = new TwinSearch();

            finder.concat(twinSearch, properties, null);

            assertNotNull(twinSearch.getDstLinksAllOfList());
            assertTrue(twinSearch.getDstLinksAllOfList().containsKey(linkId));
            assertTrue(twinSearch.getDstLinksAllOfList().get(linkId).isEmpty());
        }

        @Test
        void concat_notExcludeAnyOf_addsToDstLinksAnyOfList() throws ServiceException {
            var linkId = UUID.randomUUID();

            var properties = new Properties();
            properties.setProperty("linkId", linkId.toString());
            properties.setProperty("exclude", "false");
            properties.setProperty("anyOfList", "true");
            var twinSearch = new TwinSearch();

            finder.concat(twinSearch, properties, null);

            assertNotNull(twinSearch.getDstLinksAnyOfList());
            assertTrue(twinSearch.getDstLinksAnyOfList().containsKey(linkId));
        }

        @Test
        void concat_excludeAllOf_addsToDstLinksNoAllOfList() throws ServiceException {
            var linkId = UUID.randomUUID();

            var properties = new Properties();
            properties.setProperty("linkId", linkId.toString());
            properties.setProperty("exclude", "true");
            properties.setProperty("anyOfList", "false");
            var twinSearch = new TwinSearch();

            finder.concat(twinSearch, properties, null);

            assertNotNull(twinSearch.getDstLinksNoAllOfList());
            assertTrue(twinSearch.getDstLinksNoAllOfList().containsKey(linkId));
        }

        @Test
        void concat_excludeAnyOf_addsToDstLinksNoAnyOfList() throws ServiceException {
            var linkId = UUID.randomUUID();

            var properties = new Properties();
            properties.setProperty("linkId", linkId.toString());
            properties.setProperty("exclude", "true");
            properties.setProperty("anyOfList", "true");
            var twinSearch = new TwinSearch();

            finder.concat(twinSearch, properties, null);

            assertNotNull(twinSearch.getDstLinksNoAnyOfList());
            assertTrue(twinSearch.getDstLinksNoAnyOfList().containsKey(linkId));
        }
    }
}
