package org.twins.core.featurer.classfield.finder;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.domain.search.TwinClassFieldSearch;

import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FieldFinderByClassIdGivenTest extends BaseUnitTest {

    private final FieldFinderByClassIdGiven finder = new FieldFinderByClassIdGiven();

    @Nested
    class ConcatSearch {

        @Test
        void concatSearch_singleClassId_addsToSearch() throws ServiceException {
            var classId = UUID.randomUUID();
            var properties = new Properties();
            properties.setProperty("classIds", classId.toString());
            properties.setProperty("excludeClassesIds", "false");
            properties.setProperty("searchExtends", "false");
            var search = new TwinClassFieldSearch();

            finder.concatSearch(properties, search, java.util.Map.of());

            assertNotNull(search.getTwinClassIdMap());
            assertTrue(search.getTwinClassIdMap().containsKey(classId));
            assertFalse(search.getTwinClassIdMap().get(classId));
        }

        @Test
        void concatSearch_multipleClassIds_addsAllToSearch() throws ServiceException {
            var classId1 = UUID.randomUUID();
            var classId2 = UUID.randomUUID();
            var properties = new Properties();
            properties.setProperty("classIds", classId1.toString() + ", " + classId2.toString());
            properties.setProperty("excludeClassesIds", "false");
            properties.setProperty("searchExtends", "false");
            var search = new TwinClassFieldSearch();

            finder.concatSearch(properties, search, java.util.Map.of());

            assertNotNull(search.getTwinClassIdMap());
            assertTrue(search.getTwinClassIdMap().containsKey(classId1));
            assertTrue(search.getTwinClassIdMap().containsKey(classId2));
        }

        @Test
        void concatSearch_excludeClassesIdsTrue_addsToExcludeMap() throws ServiceException {
            var classId = UUID.randomUUID();
            var properties = new Properties();
            properties.setProperty("classIds", classId.toString());
            properties.setProperty("excludeClassesIds", "true");
            properties.setProperty("searchExtends", "false");
            var search = new TwinClassFieldSearch();

            finder.concatSearch(properties, search, java.util.Map.of());

            assertNotNull(search.getTwinClassIdExcludeMap());
            assertTrue(search.getTwinClassIdExcludeMap().containsKey(classId));
            assertNull(search.getTwinClassIdMap());
        }

        @Test
        void concatSearch_searchExtendsTrue_setsExtendsFlag() throws ServiceException {
            var classId = UUID.randomUUID();
            var properties = new Properties();
            properties.setProperty("classIds", classId.toString());
            properties.setProperty("excludeClassesIds", "false");
            properties.setProperty("searchExtends", "true");
            var search = new TwinClassFieldSearch();

            finder.concatSearch(properties, search, java.util.Map.of());

            assertTrue(search.getTwinClassIdMap().get(classId));
        }

        @Test
        void concatSearch_excludeWithExtends_addsToExcludeMapWithExtends() throws ServiceException {
            var classId = UUID.randomUUID();
            var properties = new Properties();
            properties.setProperty("classIds", classId.toString());
            properties.setProperty("excludeClassesIds", "true");
            properties.setProperty("searchExtends", "true");
            var search = new TwinClassFieldSearch();

            finder.concatSearch(properties, search, java.util.Map.of());

            assertTrue(search.getTwinClassIdExcludeMap().get(classId));
        }
    }
}
