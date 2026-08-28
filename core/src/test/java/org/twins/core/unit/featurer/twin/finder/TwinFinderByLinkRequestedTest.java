package org.twins.core.featurer.twin.finder;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.domain.search.TwinSearch;
import org.twins.core.exception.ErrorCodeTwins;

import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TwinFinderByLinkRequestedTest extends BaseUnitTest {

    private final TwinFinderByLinkRequested finder = new TwinFinderByLinkRequested();

    @Nested
    class Concat {

        @Test
        void concat_withLinkId_addsToDstLinksAllOfList() throws ServiceException {
            var linkId = UUID.randomUUID();

            var properties = new Properties();
            properties.setProperty("paramKey", "linkId");
            properties.setProperty("exclude", "false");
            properties.setProperty("anyOfList", "false");
            var twinSearch = new TwinSearch();
            var namedParams = Map.of("linkId", linkId.toString());

            finder.concat(twinSearch, properties, namedParams);

            assertNotNull(twinSearch.getDstLinksAllOfList());
            assertTrue(twinSearch.getDstLinksAllOfList().containsKey(linkId));
        }

        @Test
        void concat_anyOfListTrue_addsToDstLinksAnyOfList() throws ServiceException {
            var linkId = UUID.randomUUID();

            var properties = new Properties();
            properties.setProperty("paramKey", "linkId");
            properties.setProperty("exclude", "false");
            properties.setProperty("anyOfList", "true");
            var twinSearch = new TwinSearch();
            var namedParams = Map.of("linkId", linkId.toString());

            finder.concat(twinSearch, properties, namedParams);

            assertNotNull(twinSearch.getDstLinksAnyOfList());
            assertTrue(twinSearch.getDstLinksAnyOfList().containsKey(linkId));
        }

        @Test
        void concat_nullLinkId_doesNotModifySearch() throws ServiceException {
            var properties = new Properties();
            properties.setProperty("paramKey", "linkId");
            properties.setProperty("exclude", "false");
            properties.setProperty("anyOfList", "false");
            properties.setProperty("required", "false");
            var twinSearch = new TwinSearch();

            finder.concat(twinSearch, properties, Map.of());

            assertNull(twinSearch.getDstLinksAllOfList());
            assertNull(twinSearch.getDstLinksAnyOfList());
        }

        @Test
        void concat_requiredButMissing_throwsException() {
            var properties = new Properties();
            properties.setProperty("paramKey", "linkId");
            properties.setProperty("exclude", "false");
            properties.setProperty("anyOfList", "false");
            properties.setProperty("required", "true");
            var twinSearch = new TwinSearch();

            var ex = assertThrows(
                    org.cambium.common.exception.ServiceException.class,
                    () -> finder.concat(twinSearch, properties, Map.of())
            );
            assertEquals(ErrorCodeTwins.TWIN_SEARCH_PARAM_MISSED.getCode(), ex.getErrorCode());
        }

        @Test
        void concat_invalidUuid_throwsException() {
            var properties = new Properties();
            properties.setProperty("paramKey", "linkId");
            properties.setProperty("exclude", "false");
            properties.setProperty("anyOfList", "false");
            var twinSearch = new TwinSearch();
            var namedParams = Map.of("linkId", "not-a-uuid");

            var ex = assertThrows(
                    org.cambium.common.exception.ServiceException.class,
                    () -> finder.concat(twinSearch, properties, namedParams)
            );
            assertEquals(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT.getCode(), ex.getErrorCode());
        }

        @Test
        void concat_excludeTrueAllOf_addsToDstLinksNoAllOfList() throws ServiceException {
            var linkId = UUID.randomUUID();

            var properties = new Properties();
            properties.setProperty("paramKey", "linkId");
            properties.setProperty("exclude", "true");
            properties.setProperty("anyOfList", "false");
            var twinSearch = new TwinSearch();
            var namedParams = Map.of("linkId", linkId.toString());

            finder.concat(twinSearch, properties, namedParams);

            assertNotNull(twinSearch.getDstLinksNoAllOfList());
            assertTrue(twinSearch.getDstLinksNoAllOfList().containsKey(linkId));
        }
    }
}
