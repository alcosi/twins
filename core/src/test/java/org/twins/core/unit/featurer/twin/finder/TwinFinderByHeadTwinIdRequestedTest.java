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

class TwinFinderByHeadTwinIdRequestedTest extends BaseUnitTest {

    private final TwinFinderByHeadTwinIdRequested finder = new TwinFinderByHeadTwinIdRequested();

    @Nested
    class Concat {

        @Test
        void concat_withHeadTwinId_addsToSearch() throws ServiceException {
            var headTwinId = UUID.randomUUID();

            var properties = new Properties();
            properties.setProperty("paramKey", "twinId");
            var twinSearch = new TwinSearch();
            var namedParams = Map.of("twinId", headTwinId.toString());

            finder.concat(twinSearch, properties, namedParams);

            assertNotNull(twinSearch.getHeadTwinIdList());
            assertTrue(twinSearch.getHeadTwinIdList().contains(headTwinId));
        }

        @Test
        void concat_nullHeadTwinId_doesNotModifySearch() throws ServiceException {
            var properties = new Properties();
            properties.setProperty("paramKey", "twinId");
            properties.setProperty("required", "false");
            var twinSearch = new TwinSearch();

            finder.concat(twinSearch, properties, Map.of());

            assertNull(twinSearch.getHeadTwinIdList());
        }

        @Test
        void concat_blankHeadTwinId_doesNotModifySearch() throws ServiceException {
            var properties = new Properties();
            properties.setProperty("paramKey", "twinId");
            properties.setProperty("required", "false");
            var twinSearch = new TwinSearch();
            var namedParams = Map.of("twinId", "");

            finder.concat(twinSearch, properties, namedParams);

            assertNull(twinSearch.getHeadTwinIdList());
        }

        @Test
        void concat_requiredButMissing_throwsException() {
            var properties = new Properties();
            properties.setProperty("paramKey", "twinId");
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
            properties.setProperty("paramKey", "twinId");
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
