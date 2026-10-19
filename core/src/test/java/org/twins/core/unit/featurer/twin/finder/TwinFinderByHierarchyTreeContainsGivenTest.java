package org.twins.core.featurer.twin.finder;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.domain.search.TwinSearch;

import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TwinFinderByHierarchyTreeContainsGivenTest extends BaseUnitTest {

    private final TwinFinderByHierarchyTreeContainsGiven finder = new TwinFinderByHierarchyTreeContainsGiven();

    @Nested
    class Concat {

        @Test
        void concat_addsHierarchyTreeContainsId() throws ServiceException {
            var twinId = UUID.randomUUID();

            var properties = new Properties();
            properties.setProperty("twinIds", twinId.toString());
            var twinSearch = new TwinSearch();

            finder.concat(twinSearch, properties, null);

            assertNotNull(twinSearch.getHierarchyTreeContainsIdList());
            assertTrue(twinSearch.getHierarchyTreeContainsIdList().contains(twinId));
        }

        @Test
        void concat_emptyTwinSearch_beforeConcat() throws ServiceException {
            var twinId = UUID.randomUUID();

            var properties = new Properties();
            properties.setProperty("twinIds", twinId.toString());
            var twinSearch = new TwinSearch();

            assertTrue(twinSearch.isEmpty());

            finder.concat(twinSearch, properties, null);

            assertFalse(twinSearch.isEmpty());
        }
    }
}
