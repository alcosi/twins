package org.twins.core.featurer.classfinder;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.domain.search.HierarchySearch;
import org.twins.core.domain.search.TwinClassSearch;

import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


class ClassFinderExtendsHierarchyChildrenOfTest extends BaseUnitTest {

    private final ClassFinderExtendsHierarchyChildrenOf classFinder = new ClassFinderExtendsHierarchyChildrenOf();

    @Nested
    class ConcatSearch {

        @Test
        void concatSearch_setsExtendsHierarchyChildsSearch() throws ServiceException {
            var twinClassId = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
            var properties = new Properties();
            properties.setProperty("twinClassId", twinClassId.toString());
            properties.setProperty("extendsDepth", "3");
            var classSearch = new TwinClassSearch();

            classFinder.concatSearch(properties, classSearch);

            HierarchySearch extendsSearch = classSearch.getExtendsHierarchyChildsForTwinClassSearch();
            assertNotNull(extendsSearch);
            assertTrue(extendsSearch.getIdList().contains(twinClassId));
            assertEquals(3, extendsSearch.getDepth());
        }

        @Test
        void concatSearch_defaultDepth_setsZeroDepth() throws ServiceException {
            var twinClassId = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
            var properties = new Properties();
            properties.setProperty("twinClassId", twinClassId.toString());
            properties.setProperty("extendsDepth", "0");
            var classSearch = new TwinClassSearch();

            classFinder.concatSearch(properties, classSearch);

            HierarchySearch extendsSearch = classSearch.getExtendsHierarchyChildsForTwinClassSearch();
            assertNotNull(extendsSearch);
            assertEquals(0, extendsSearch.getDepth());
        }
    }
}
