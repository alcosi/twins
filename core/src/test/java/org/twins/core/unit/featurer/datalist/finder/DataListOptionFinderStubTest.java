package org.twins.core.featurer.datalist.finder;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.domain.search.DataListOptionSearch;
import org.twins.core.featurer.datalist.finder.DataListOptionFinderStub;

import java.util.Properties;
import static org.junit.jupiter.api.Assertions.*;


class DataListOptionFinderStubTest extends BaseUnitTest {

    private final DataListOptionFinderStub finder = new DataListOptionFinderStub();

    @Nested
    class ConcatSearch {

        @Test
        void concatSearch_doesNotModifySearch() throws ServiceException {
            var search = new DataListOptionSearch();

            finder.concatSearch(new Properties(), search, java.util.Map.of());

            assertNull(search.getIdList());
            assertNull(search.getDataListIdList());
        }
    }
}
