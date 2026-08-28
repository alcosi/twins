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

class TwinFinderByClassIdRequestedTest extends BaseUnitTest {

    private final TwinFinderByClassIdRequested finder = new TwinFinderByClassIdRequested() {};

    @Nested
    class Concat {

        @Test
        void concat_singleClassId_addsToSearch() throws ServiceException {
            var classId = UUID.randomUUID();

            var properties = new Properties();
            properties.setProperty("paramKey", "twinClassId");
            properties.setProperty("exclude", "false");
            var twinSearch = new TwinSearch();
            var namedParams = Map.of("twinClassId", classId.toString());

            finder.concat(twinSearch, properties, namedParams);

            assertNotNull(twinSearch.getTwinClassIdList());
            assertTrue(twinSearch.getTwinClassIdList().contains(classId));
        }

        @Test
        void concat_multipleClassIds_addsToSearch() throws ServiceException {
            var classId1 = UUID.randomUUID();
            var classId2 = UUID.randomUUID();

            var properties = new Properties();
            properties.setProperty("paramKey", "twinClassId");
            properties.setProperty("exclude", "false");
            var twinSearch = new TwinSearch();
            var namedParams = Map.of("twinClassId", classId1 + "," + classId2);

            finder.concat(twinSearch, properties, namedParams);

            assertNotNull(twinSearch.getTwinClassIdList());
            assertEquals(2, twinSearch.getTwinClassIdList().size());
        }

        @Test
        void concat_excludeTrue_addsToClassIdExcludeList() throws ServiceException {
            var classId = UUID.randomUUID();

            var properties = new Properties();
            properties.setProperty("paramKey", "twinClassId");
            properties.setProperty("exclude", "true");
            var twinSearch = new TwinSearch();
            var namedParams = Map.of("twinClassId", classId.toString());

            finder.concat(twinSearch, properties, namedParams);

            assertNotNull(twinSearch.getTwinClassIdExcludeList());
            assertTrue(twinSearch.getTwinClassIdExcludeList().contains(classId));
            assertNull(twinSearch.getTwinClassIdList());
        }

        @Test
        void concat_emptyClassId_returnsWithoutModification() throws ServiceException {
            var properties = new Properties();
            properties.setProperty("paramKey", "twinClassId");
            properties.setProperty("exclude", "false");
            var twinSearch = new TwinSearch();
            var namedParams = Map.of("twinClassId", "");

            finder.concat(twinSearch, properties, namedParams);

            assertNull(twinSearch.getTwinClassIdList());
            assertNull(twinSearch.getTwinClassIdExcludeList());
        }

        @Test
        void concat_missingClassIdNotRequired_returnsWithoutModification() throws ServiceException {
            var properties = new Properties();
            properties.setProperty("paramKey", "twinClassId");
            properties.setProperty("exclude", "false");
            properties.setProperty("required", "false");
            var twinSearch = new TwinSearch();

            finder.concat(twinSearch, properties, Map.of());

            assertNull(twinSearch.getTwinClassIdList());
            assertNull(twinSearch.getTwinClassIdExcludeList());
        }
    }
}
