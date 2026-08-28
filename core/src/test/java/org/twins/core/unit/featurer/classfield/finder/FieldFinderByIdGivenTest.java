package org.twins.core.featurer.classfield.finder;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.domain.search.TwinClassFieldSearch;

import java.util.HashSet;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FieldFinderByIdGivenTest extends BaseUnitTest {

    private final FieldFinderByIdGiven finder = new FieldFinderByIdGiven();

    @Nested
    class ConcatSearch {

        @Test
        void concatSearch_singleFieldId_addsToIdList() throws ServiceException {
            var fieldId = UUID.randomUUID();
            var properties = new Properties();
            properties.setProperty("fieldIds", fieldId.toString());
            properties.setProperty("excludeGivenIds", "false");
            var search = new TwinClassFieldSearch();

            finder.concatSearch(properties, search, java.util.Map.of());

            assertNotNull(search.getIdList());
            assertTrue(search.getIdList().contains(fieldId));
        }

        @Test
        void concatSearch_multipleFieldIds_addsAllToIdList() throws ServiceException {
            var fieldId1 = UUID.randomUUID();
            var fieldId2 = UUID.randomUUID();
            var properties = new Properties();
            properties.setProperty("fieldIds", fieldId1.toString() + ", " + fieldId2.toString());
            properties.setProperty("excludeGivenIds", "false");
            var search = new TwinClassFieldSearch();

            finder.concatSearch(properties, search, java.util.Map.of());

            assertEquals(2, search.getIdList().size());
            assertTrue(search.getIdList().contains(fieldId1));
            assertTrue(search.getIdList().contains(fieldId2));
        }

        @Test
        void concatSearch_excludeGivenIdsTrue_addsToExcludeList() throws ServiceException {
            var fieldId = UUID.randomUUID();
            var properties = new Properties();
            properties.setProperty("fieldIds", fieldId.toString());
            properties.setProperty("excludeGivenIds", "true");
            var search = new TwinClassFieldSearch();

            finder.concatSearch(properties, search, java.util.Map.of());

            assertNull(search.getIdList());
            assertNotNull(search.getIdExcludeList());
            assertTrue(search.getIdExcludeList().contains(fieldId));
        }

        @Test
        void concatSearch_excludeGivenIdsTrue_appendsToExistingExcludeList() throws ServiceException {
            var existingId = UUID.randomUUID();
            var newId = UUID.randomUUID();
            var properties = new Properties();
            properties.setProperty("fieldIds", newId.toString());
            properties.setProperty("excludeGivenIds", "true");
            var search = new TwinClassFieldSearch();
            search.setIdExcludeList(new HashSet<>(java.util.Set.of(existingId)));

            finder.concatSearch(properties, search, java.util.Map.of());

            assertTrue(search.getIdExcludeList().contains(existingId));
            assertTrue(search.getIdExcludeList().contains(newId));
        }

        @Test
        void concatSearch_appendsToExistingIdList() throws ServiceException {
            var existingId = UUID.randomUUID();
            var newId = UUID.randomUUID();
            var properties = new Properties();
            properties.setProperty("fieldIds", newId.toString());
            properties.setProperty("excludeGivenIds", "false");
            var search = new TwinClassFieldSearch();
            search.setIdList(new HashSet<>(java.util.Set.of(existingId)));

            finder.concatSearch(properties, search, java.util.Map.of());

            assertTrue(search.getIdList().contains(existingId));
            assertTrue(search.getIdList().contains(newId));
        }
    }
}
