package org.twins.core.featurer.classfinder;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.domain.search.TwinClassSearch;

import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


class ClassFinderGivenSetTest extends BaseUnitTest {

    private final ClassFinderGivenSet classFinder = new ClassFinderGivenSet();

    @Nested
    class ConcatSearch {

        @Test
        void concatSearch_excludeFalse_addsToTwinClassIdList() throws ServiceException {
            var id1 = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
            var id2 = UUID.fromString("11111111-2222-3333-4444-555555555555");
            var properties = new Properties();
            properties.setProperty("twinClassIdSet", id1 + ", " + id2);
            properties.setProperty("excludeGivenIds", "false");
            var classSearch = new TwinClassSearch();

            classFinder.concatSearch(properties, classSearch);

            assertNotNull(classSearch.getTwinClassIdList());
            assertEquals(2, classSearch.getTwinClassIdList().size());
            assertTrue(classSearch.getTwinClassIdList().contains(id1));
            assertTrue(classSearch.getTwinClassIdList().contains(id2));
            assertNull(classSearch.getTwinClassIdExcludeList());
        }

        @Test
        void concatSearch_excludeTrue_addsToTwinClassIdExcludeList() throws ServiceException {
            var id1 = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
            var properties = new Properties();
            properties.setProperty("twinClassIdSet", id1.toString());
            properties.setProperty("excludeGivenIds", "true");
            var classSearch = new TwinClassSearch();

            classFinder.concatSearch(properties, classSearch);

            assertNotNull(classSearch.getTwinClassIdExcludeList());
            assertEquals(1, classSearch.getTwinClassIdExcludeList().size());
            assertTrue(classSearch.getTwinClassIdExcludeList().contains(id1));
            assertNull(classSearch.getTwinClassIdList());
        }

        @Test
        void concatSearch_emptySet_doesNotModifySearch() throws ServiceException {
            var properties = new Properties();
            properties.setProperty("twinClassIdSet", "");
            properties.setProperty("excludeGivenIds", "false");
            var classSearch = new TwinClassSearch();

            classFinder.concatSearch(properties, classSearch);

            assertNull(classSearch.getTwinClassIdList());
            assertNull(classSearch.getTwinClassIdExcludeList());
        }

        @Test
        void concatSearch_singleId_addsSingleId() throws ServiceException {
            var id1 = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
            var properties = new Properties();
            properties.setProperty("twinClassIdSet", id1.toString());
            properties.setProperty("excludeGivenIds", "false");
            var classSearch = new TwinClassSearch();

            classFinder.concatSearch(properties, classSearch);

            assertNotNull(classSearch.getTwinClassIdList());
            assertEquals(1, classSearch.getTwinClassIdList().size());
            assertTrue(classSearch.getTwinClassIdList().contains(id1));
        }
    }
}
