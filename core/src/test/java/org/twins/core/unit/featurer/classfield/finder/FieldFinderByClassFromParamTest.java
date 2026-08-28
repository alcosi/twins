package org.twins.core.featurer.classfield.finder;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.domain.search.TwinClassFieldSearch;
import org.twins.core.exception.ErrorCodeTwins;

import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FieldFinderByClassFromParamTest extends BaseUnitTest {

    private final FieldFinderByClassFromParam finder = new FieldFinderByClassFromParam();

    @Nested
    class ConcatSearch {

        @Test
        void concatSearch_validClassId_addsTwinClassId() throws ServiceException {
            var classId = UUID.randomUUID();
            var properties = new Properties();
            properties.setProperty("searchExtends", "false");
            var search = new TwinClassFieldSearch();
            var namedParams = Map.of(FieldFinder.PARAM_CURRENT_TWIN_CLASS_ID, classId.toString());

            finder.concatSearch(properties, search, namedParams);

            assertNotNull(search.getTwinClassIdMap());
            assertTrue(search.getTwinClassIdMap().containsKey(classId));
            assertFalse(search.getTwinClassIdMap().get(classId));
        }

        @Test
        void concatSearch_searchExtendsTrue_setsExtendsFlag() throws ServiceException {
            var classId = UUID.randomUUID();
            var properties = new Properties();
            properties.setProperty("searchExtends", "true");
            var search = new TwinClassFieldSearch();
            var namedParams = Map.of(FieldFinder.PARAM_CURRENT_TWIN_CLASS_ID, classId.toString());

            finder.concatSearch(properties, search, namedParams);

            assertNotNull(search.getTwinClassIdMap());
            assertTrue(search.getTwinClassIdMap().containsKey(classId));
            assertTrue(search.getTwinClassIdMap().get(classId));
        }

        @Test
        void concatSearch_missingParam_throwsException() {
            var properties = new Properties();
            var search = new TwinClassFieldSearch();

            var ex = assertThrows(
                    ServiceException.class,
                    () -> finder.concatSearch(properties, search, Map.of())
            );
            assertEquals(ErrorCodeTwins.TWIN_SEARCH_PARAM_MISSED.getCode(), ex.getErrorCode());
        }

        @Test
        void concatSearch_blankParam_throwsException() {
            var properties = new Properties();
            var search = new TwinClassFieldSearch();
            var namedParams = Map.of(FieldFinder.PARAM_CURRENT_TWIN_CLASS_ID, "");

            var ex = assertThrows(
                    ServiceException.class,
                    () -> finder.concatSearch(properties, search, namedParams)
            );
            assertEquals(ErrorCodeTwins.TWIN_SEARCH_PARAM_MISSED.getCode(), ex.getErrorCode());
        }

        @Test
        void concatSearch_nonUuidParam_throwsException() {
            var properties = new Properties();
            var search = new TwinClassFieldSearch();
            var namedParams = Map.of(FieldFinder.PARAM_CURRENT_TWIN_CLASS_ID, "not-a-uuid");

            var ex = assertThrows(
                    ServiceException.class,
                    () -> finder.concatSearch(properties, search, namedParams)
            );
            assertEquals(ErrorCodeTwins.TWIN_SEARCH_PARAM_MISSED.getCode(), ex.getErrorCode());
        }
    }
}
