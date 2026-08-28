package org.twins.core.featurer.datalist.sorter;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.featurer.datalist.sorter.DataListOptionSorterStub;

import java.util.Properties;
import static org.junit.jupiter.api.Assertions.*;


class DataListOptionSorterStubTest extends BaseUnitTest {

    private final DataListOptionSorterStub sorter = new DataListOptionSorterStub();

    @Nested
    class CreateSort {

        @Test
        void createSort_returnsNull() throws ServiceException {
            assertNull(sorter.createSort(new Properties()));
        }
    }
}
