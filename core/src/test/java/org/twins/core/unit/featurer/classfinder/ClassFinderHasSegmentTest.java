package org.twins.core.featurer.classfinder;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.Ternary;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.domain.search.TwinClassSearch;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;


class ClassFinderHasSegmentTest extends BaseUnitTest {

    private final ClassFinderHasSegment classFinder = new ClassFinderHasSegment();

    @Nested
    class ConcatSearch {

        @Test
        void concatSearch_excludeTrue_setsAbstracttToOnlyNot() throws ServiceException {
            var properties = new Properties();
            properties.setProperty("exclude", "true");
            var classSearch = new TwinClassSearch();

            classFinder.concatSearch(properties, classSearch);

            assertEquals(Ternary.ONLY_NOT, classSearch.getAbstractt());
        }

        @Test
        void concatSearch_excludeFalse_setsAbstracttToOnly() throws ServiceException {
            var properties = new Properties();
            properties.setProperty("exclude", "false");
            var classSearch = new TwinClassSearch();

            classFinder.concatSearch(properties, classSearch);

            assertEquals(Ternary.ONLY, classSearch.getAbstractt());
        }
    }
}
