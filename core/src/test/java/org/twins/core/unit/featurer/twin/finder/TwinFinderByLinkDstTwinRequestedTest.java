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

class TwinFinderByLinkDstTwinRequestedTest extends BaseUnitTest {

    private final TwinFinderByLinkDstTwinRequested finder = new TwinFinderByLinkDstTwinRequested();

    @Nested
    class Concat {

        @Test
        void concat_withDstTwinId_addsToDstLinksAllOfList() throws ServiceException {
            var linkId = UUID.randomUUID();
            var dstTwinId = UUID.randomUUID();

            var properties = new Properties();
            properties.setProperty("paramKey", "twinId");
            properties.setProperty("linkId", linkId.toString());
            properties.setProperty("exclude", "false");
            properties.setProperty("anyOfList", "false");
            var twinSearch = new TwinSearch();
            var namedParams = Map.of("twinId", dstTwinId.toString());

            finder.concat(twinSearch, properties, namedParams);

            assertNotNull(twinSearch.getDstLinksAllOfList());
            assertTrue(twinSearch.getDstLinksAllOfList().containsKey(linkId));
            assertTrue(twinSearch.getDstLinksAllOfList().get(linkId).contains(dstTwinId));
        }

        @Test
        void concat_anyOfListTrue_addsToDstLinksAnyOfList() throws ServiceException {
            var linkId = UUID.randomUUID();
            var dstTwinId = UUID.randomUUID();

            var properties = new Properties();
            properties.setProperty("paramKey", "twinId");
            properties.setProperty("linkId", linkId.toString());
            properties.setProperty("exclude", "false");
            properties.setProperty("anyOfList", "true");
            var twinSearch = new TwinSearch();
            var namedParams = Map.of("twinId", dstTwinId.toString());

            finder.concat(twinSearch, properties, namedParams);

            assertNotNull(twinSearch.getDstLinksAnyOfList());
            assertTrue(twinSearch.getDstLinksAnyOfList().containsKey(linkId));
            assertTrue(twinSearch.getDstLinksAnyOfList().get(linkId).contains(dstTwinId));
        }

        @Test
        void concat_excludeTrueAllOf_addsToDstLinksNoAllOfList() throws ServiceException {
            var linkId = UUID.randomUUID();
            var dstTwinId = UUID.randomUUID();

            var properties = new Properties();
            properties.setProperty("paramKey", "twinId");
            properties.setProperty("linkId", linkId.toString());
            properties.setProperty("exclude", "true");
            properties.setProperty("anyOfList", "false");
            var twinSearch = new TwinSearch();
            var namedParams = Map.of("twinId", dstTwinId.toString());

            finder.concat(twinSearch, properties, namedParams);

            assertNotNull(twinSearch.getDstLinksNoAllOfList());
            assertTrue(twinSearch.getDstLinksNoAllOfList().containsKey(linkId));
        }

        @Test
        void concat_requiredButMissingTwinId_throwsException() {
            var linkId = UUID.randomUUID();

            var properties = new Properties();
            properties.setProperty("paramKey", "twinId");
            properties.setProperty("linkId", linkId.toString());
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
        void concat_invalidUuidTwinId_throwsException() {
            var linkId = UUID.randomUUID();

            var properties = new Properties();
            properties.setProperty("paramKey", "twinId");
            properties.setProperty("linkId", linkId.toString());
            properties.setProperty("exclude", "false");
            properties.setProperty("anyOfList", "false");
            var twinSearch = new TwinSearch();
            var namedParams = Map.of("twinId", "not-a-uuid");

            var ex = assertThrows(
                    org.cambium.common.exception.ServiceException.class,
                    () -> finder.concat(twinSearch, properties, namedParams)
            );
            assertEquals(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT.getCode(), ex.getErrorCode());
        }
    }
}
