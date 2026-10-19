package org.twins.core.featurer.classfield.finder;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.domain.search.TwinClassFieldSearch;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class FieldFinderStubTest extends BaseUnitTest {

    private final FieldFinderStub finder = new FieldFinderStub();

    @Nested
    class ConcatSearch {

        @Test
        void concatSearch_setsInactiveSearchTrue() throws ServiceException {
            var search = new TwinClassFieldSearch();

            finder.concatSearch(new Properties(), search, java.util.Map.of());

            assertTrue(search.isInactiveSearch());
        }
    }
}
