package org.twins.core.featurer.user.sorter;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;


class UserSorterStubTest extends BaseUnitTest {

    private final UserSorterStub sorter = new UserSorterStub();

    @Nested
    class CreateSort {

        @Test
        void createSort_returnsNull() throws ServiceException {
            assertNull(sorter.createSort(new Properties()));
        }
    }
}
