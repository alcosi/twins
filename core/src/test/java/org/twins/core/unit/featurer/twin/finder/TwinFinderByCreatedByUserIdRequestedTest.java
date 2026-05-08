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

class TwinFinderByCreatedByUserIdRequestedTest extends BaseUnitTest {

    private final TwinFinderByCreatedByUserIdRequested finder = new TwinFinderByCreatedByUserIdRequested() {};

    @Nested
    class Concat {

        @Test
        void concat_singleUserId_addsToSearch() throws ServiceException {
            var userId = UUID.randomUUID();

            var properties = new Properties();
            properties.setProperty("paramKey", "userId");
            properties.setProperty("exclude", "false");
            var twinSearch = new TwinSearch();
            var namedParams = Map.of("userId", userId.toString());

            finder.concat(twinSearch, properties, namedParams);

            assertNotNull(twinSearch.getCreatedByUserIdExcludeList());
            assertTrue(twinSearch.getCreatedByUserIdExcludeList().contains(userId));
        }

        @Test
        void concat_multipleUserIds_addsToSearch() throws ServiceException {
            var userId1 = UUID.randomUUID();
            var userId2 = UUID.randomUUID();

            var properties = new Properties();
            properties.setProperty("paramKey", "userId");
            properties.setProperty("exclude", "false");
            var twinSearch = new TwinSearch();
            var namedParams = Map.of("userId", userId1 + "," + userId2);

            finder.concat(twinSearch, properties, namedParams);

            assertNotNull(twinSearch.getCreatedByUserIdExcludeList());
            assertEquals(2, twinSearch.getCreatedByUserIdExcludeList().size());
        }

        @Test
        void concat_excludeTrue_addsToCreatedByUserIdList() throws ServiceException {
            var userId = UUID.randomUUID();

            var properties = new Properties();
            properties.setProperty("paramKey", "userId");
            properties.setProperty("exclude", "true");
            var twinSearch = new TwinSearch();
            var namedParams = Map.of("userId", userId.toString());

            finder.concat(twinSearch, properties, namedParams);

            assertNotNull(twinSearch.getCreatedByUserIdList());
            assertTrue(twinSearch.getCreatedByUserIdList().contains(userId));
        }

        @Test
        void concat_emptyUserId_returnsWithoutModification() throws ServiceException {
            var properties = new Properties();
            properties.setProperty("paramKey", "userId");
            properties.setProperty("exclude", "false");
            var twinSearch = new TwinSearch();
            var namedParams = Map.of("userId", "");

            finder.concat(twinSearch, properties, namedParams);

            assertNull(twinSearch.getCreatedByUserIdList());
            assertNull(twinSearch.getCreatedByUserIdExcludeList());
        }

        @Test
        void concat_missingUserIdNotRequired_returnsWithoutModification() throws ServiceException {
            var properties = new Properties();
            properties.setProperty("paramKey", "userId");
            properties.setProperty("exclude", "false");
            properties.setProperty("required", "false");
            var twinSearch = new TwinSearch();

            finder.concat(twinSearch, properties, Map.of());

            assertNull(twinSearch.getCreatedByUserIdList());
            assertNull(twinSearch.getCreatedByUserIdExcludeList());
        }
    }
}
