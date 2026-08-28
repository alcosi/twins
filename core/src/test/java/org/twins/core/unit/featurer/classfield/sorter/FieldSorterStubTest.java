package org.twins.core.featurer.classfield.sorter;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;


class FieldSorterStubTest extends BaseUnitTest {

    private final FieldSorterStub sorter = new FieldSorterStub();

    @Nested
    class CreateSort {

        @Test
        void createSort_returnsNull() throws ServiceException {
            assertNull(sorter.createSort(new Properties()));
        }
    }
}
