package org.twins.core.featurer.classfinder;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.Ternary;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.domain.search.TwinClassSearch;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;


class ClassFinderAbstractTest extends BaseUnitTest {

    private final ClassFinderAbstract classFinder = new ClassFinderAbstract();

    @Nested
    class ConcatSearch {

        @Test
        void concatSearch_excludeAbstractTrue_setsAbstracttToOnlyNot() throws ServiceException {
            var properties = new Properties();
            properties.setProperty("excludeAbstract", "true");
            var classSearch = new TwinClassSearch();

            classFinder.concatSearch(properties, classSearch);

            assertEquals(Ternary.ONLY_NOT, classSearch.getAbstractt());
        }

        @Test
        void concatSearch_excludeAbstractFalse_setsAbstracttToOnly() throws ServiceException {
            var properties = new Properties();
            properties.setProperty("excludeAbstract", "false");
            var classSearch = new TwinClassSearch();

            classFinder.concatSearch(properties, classSearch);

            assertEquals(Ternary.ONLY, classSearch.getAbstractt());
        }

        @Test
        void concatSearch_defaultValue_setsAbstracttToOnlyNot() throws ServiceException {
            var properties = new Properties();
            properties.setProperty("excludeAbstract", "true");
            var classSearch = new TwinClassSearch();

            classFinder.concatSearch(properties, classSearch);

            assertEquals(Ternary.ONLY_NOT, classSearch.getAbstractt());
        }
    }
}
