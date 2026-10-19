package org.twins.core.featurer.twin.finder;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.domain.search.TwinSearch;

import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TwinFinderByClassIdGivenTest extends BaseUnitTest {

    private final TwinFinderByClassIdGiven finder = new TwinFinderByClassIdGiven();

    @Nested
    class Concat {

        @Test
        void concat_excludeFalse_addsToClassIdList() throws ServiceException {
            var classId1 = UUID.randomUUID();
            var classId2 = UUID.randomUUID();

            var properties = new Properties();
            properties.setProperty("classIds", classId1 + "," + classId2);
            properties.setProperty("exclude", "false");
            var twinSearch = new TwinSearch();

            finder.concat(twinSearch, properties, null);

            assertNotNull(twinSearch.getTwinClassIdList());
            assertTrue(twinSearch.getTwinClassIdList().contains(classId1));
            assertTrue(twinSearch.getTwinClassIdList().contains(classId2));
        }

        @Test
        void concat_excludeTrue_addsToClassIdExcludeList() throws ServiceException {
            var classId = UUID.randomUUID();

            var properties = new Properties();
            properties.setProperty("classIds", classId.toString());
            properties.setProperty("exclude", "true");
            var twinSearch = new TwinSearch();

            finder.concat(twinSearch, properties, null);

            assertNotNull(twinSearch.getTwinClassIdExcludeList());
            assertTrue(twinSearch.getTwinClassIdExcludeList().contains(classId));
            assertNull(twinSearch.getTwinClassIdList());
        }

        @Test
        void concat_singleClassId_works() throws ServiceException {
            var classId = UUID.randomUUID();

            var properties = new Properties();
            properties.setProperty("classIds", classId.toString());
            properties.setProperty("exclude", "false");
            var twinSearch = new TwinSearch();

            finder.concat(twinSearch, properties, null);

            assertNotNull(twinSearch.getTwinClassIdList());
            assertEquals(1, twinSearch.getTwinClassIdList().size());
        }
    }
}
