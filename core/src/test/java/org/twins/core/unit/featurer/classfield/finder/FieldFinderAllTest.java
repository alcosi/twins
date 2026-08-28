package org.twins.core.featurer.classfield.finder;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.domain.search.TwinClassFieldSearch;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class FieldFinderAllTest extends BaseUnitTest {

    private final FieldFinderAll finder = new FieldFinderAll();

    @Nested
    class ConcatSearch {

        @Test
        void concatSearch_excludeSystemFieldsTrue_setsExcludeSystemFields() throws ServiceException {
            var properties = new Properties();
            properties.setProperty("excludeSystemFields", "true");
            var search = new TwinClassFieldSearch();

            finder.concatSearch(properties, search, java.util.Map.of());

            assertTrue(search.isExcludeSystemFields());
        }

        @Test
        void concatSearch_excludeSystemFieldsFalse_keepsSystemFields() throws ServiceException {
            var properties = new Properties();
            properties.setProperty("excludeSystemFields", "false");
            var search = new TwinClassFieldSearch();

            finder.concatSearch(properties, search, java.util.Map.of());

            assertFalse(search.isExcludeSystemFields());
        }

        @Test
        void concatSearch_noProperty_setsFalse() throws ServiceException {
            var properties = new Properties();
            var search = new TwinClassFieldSearch();

            finder.concatSearch(properties, search, java.util.Map.of());

            assertFalse(search.isExcludeSystemFields());
        }
    }
}
